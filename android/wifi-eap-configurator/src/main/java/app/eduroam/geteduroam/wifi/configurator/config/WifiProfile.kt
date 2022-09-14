package app.eduroam.geteduroam.wifi.configurator.config

import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiEnterpriseConfig
import android.net.wifi.WifiNetworkSpecifier
import android.net.wifi.WifiNetworkSuggestion
import android.net.wifi.hotspot2.PasspointConfiguration
import android.net.wifi.hotspot2.pps.Credential
import android.net.wifi.hotspot2.pps.Credential.CertificateCredential
import android.net.wifi.hotspot2.pps.Credential.UserCredential
import android.net.wifi.hotspot2.pps.HomeSp
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import app.eduroam.geteduroam.wifi.configurator.exception.EapConfigCAException
import app.eduroam.geteduroam.wifi.configurator.exception.EapConfigClientCertificateException
import app.eduroam.geteduroam.wifi.configurator.exception.EapConfigValueException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.IllegalStateException
import java.nio.charset.Charset
import java.security.*
import java.security.cert.CertificateEncodingException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import java.util.function.Function
import java.util.function.IntFunction
import java.util.function.Predicate
import java.util.stream.Collectors

/**
 * Class representing a Wi-Fi profile that can be installed on any Android version
 *
 * The class contains all information necessary to configure a Wi-Fi network,
 * such as EAP types used, username/password or client certificate and EAP types.
 *
 * There are different methods for configuring a Wi-Fi network on Android, this class can output
 * configuration objects for different configuration methods.  Configuration object that can be
 * created are WifiConfiguration, WifiNetworkSuggestion and NetworkRequest
 */
class WifiProfile(config: JSONObject) {
    private val ssids: Array<String?>
    private val oids: LongArray
    private var clientCertificate: Map.Entry<PrivateKey, Array<X509Certificate>>? = null
    private var anonymousIdentity: String? = null
    private var caCertificates: List<X509Certificate>? = null
    private var enterpriseEAP = 0
    private val serverNames: Array<String?>
    private var username: String? = null
    private var password: String? = null
    private var enterprisePhase2Auth = 0
    private var fqdn: String? = null

    /**
     * Parse a JSON object with Wi-Fi configuration settings and store them in this object.
     *
     * The following fields are always required:
     * * `String[] ssid` SSIDs in UTF-8
     * * `String[] servername` Server names for server certificate validation
     * * `String[] caCertificate` Certificate chains for server certificate validation
     * * `int eap` IANA EAP code, `13` = `TLS`, `21` = TTLS, `25` = PEAP
     * * `String id` FQDN for this profile, used for Passpoint home ID matching, often identical to realm
     *
     * The following fields are required for TLS profiles
     * * `String clientCertificate` Base64 encoded PKCS12 container
     * * `String passPhrase` Passphrase to decrypt PKCS12 container
     *
     * The following fields are required for non-TLS profiles (TTLS or PEAP)
     * * `String username` Username for authentication, including @realm
     * * `String password` Password for authentication
     * * `int auth` CAT identifier for Phase2 auth (see XSD)
     *
     * The following fields are optional, but must be of the correct type if provided
     * * `String[] oid` OID hex-encoded strings for Passpoint
     * * `anonymous` Outer identity
     *
     * @param config Wi-Fi profile from ionic
     * @throws IllegalStateException               Internal error; logic error or OS bug
     * @throws EapConfigCAException                Invalid CA certificate/chain provided
     * @throws EapConfigClientCertificateException Invalid client certificate provided
     * @throws EapConfigValueException             A value is missing or fails a constraint
     * @link https://www.iana.org/assignments/eap-numbers/eap-numbers.xhtml#eap-numbers-4
     * @link https://github.com/GEANT/CAT/blob/v2.0.3/devices/xml/eap-metadata.xsd
     */
    init {
        try {
            // Required fields
            ssids = jsonArrayToStringArray(config.getJSONArray("ssid"))
            serverNames = jsonArrayToStringArray(config.getJSONArray("servername"))
            enterpriseEAP = getAndroidEAPTypeFromIanaEAPType(config.getInt("eap"))
            fqdn = config.getString("id")
            try {
                caCertificates = Arrays.stream(
                    getCertificates(
                        jsonArrayToStringArray(
                            config.getJSONArray("caCertificate")
                        )
                    )
                )
                    .filter { certificate -> // We really shouldn't expect any certificate here to NOT be a CA,
                        // CAT shows a nice red warning when you try to configure this,
                        // but experience shows that sometimes this is not enough of a deterrent.
                        // We may very well block profiles like this, but then it should be done BEFORE
                        // the user enters their username/password, not after.
                        if (certificate == null) {
                            false
                        } else {
                            isCA(certificate)
                        }
                    }.collect(Collectors.toList()) as List<X509Certificate>?
            } catch (e: CertificateException) {
                throw EapConfigCAException(
                    "Failed to parse certificate from caCertificate JSON dictionary", e
                )
            }

            // Conditional fields
            if (enterpriseEAP != WifiEnterpriseConfig.Eap.TLS) {
                username = if (config.has("username")) config.getString("username") else null
                password = if (config.has("password")) config.getString("password") else null
                enterprisePhase2Auth = getAndroidPhase2FromCATAuthMethod(config.getInt("auth"))
                clientCertificate = null
            } else {
                try {
                    clientCertificate = getClientCertificate(
                        config.getString("clientCertificate"), config.getString("passPhrase")
                    )
                } catch (e: CertificateException) {
                    throw EapConfigClientCertificateException(
                        "Unable to read client certificate", e
                    )
                } catch (e: NoSuchAlgorithmException) {
                    throw EapConfigClientCertificateException(
                        "Unknown algorithm in PKCS12 store", e
                    )
                } catch (e: UnrecoverableKeyException) {
                    throw EapConfigClientCertificateException(
                        "Unable to read client certificate key", e
                    )
                }
                username = null
                password = null
                enterprisePhase2Auth = -1
            }

            // Optional fields
            anonymousIdentity = if (config.has("anonymous")) config.getString("anonymous") else null

            oids = if (config.has("oid")) toLongPrimitive(jsonArrayToStringArray(
                config.getJSONArray("oid")
            ).map<Long>(object : Function<String, Long> {
                override fun apply(oid: String): Long {
                    return if (oid.startsWith("0x")) java.lang.Long.decode(oid) else java.lang.Long.decode(
                        "0x$oid"
                    )
                }
            }).toArray(object : IntFunction<Array<Long?>> {
                override fun apply(size: Int): Array<Long?> {
                    return arrayOfNulls(size)
                }
            })
            ) else LongArray(0)
        } catch (e: JSONException) {
            throw EapConfigValueException(e.message, e)
        } catch (e: NumberFormatException) {
            throw EapConfigValueException("OID contains invalid HEX string", e)
        }
        if (ssids.size == 0 && oids.size == 0) {
            throw EapConfigValueException("List of SSIDs and OIDs cannot both be empty")
        }
        if (serverNames.size == 0) {
            throw EapConfigValueException("Empty list of server names provided")
        }
    }

    /**
     * Create SSID-based network suggestions for this profile
     *
     * This will return one suggestion per SSID.  The resulting list is generated on the fly,
     * and may be safely modified by the caller.
     *
     * @return List of network suggestions, one per SSID
     * @see this.buildPasspointSuggestion
     * @see this.buildNetworkRequests
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    fun buildSSIDSuggestions(): List<WifiNetworkSuggestion> {
        // Initial capacity = amount of SSIDs + 1, to keep room for a a Passpoint configuration
        val enterpriseConfig = buildEnterpriseConfig()
        return Arrays.stream(ssids).map(object : Function<String?, WifiNetworkSuggestion> {
            override fun apply(ssid: String?): WifiNetworkSuggestion {
                return WifiNetworkSuggestion.Builder().setSsid((ssid)!!)
                    .setWpa2EnterpriseConfig(enterpriseConfig).build()
            }
        }).collect(Collectors.toList())
    }

    /**
     * Create Passpoint-based network suggestion for this profile
     *
     * A Passpoint suggestion can contain multiple OIDs, so the whole profile will always fit
     * in a single suggestions.
     *
     * If there are no OIDs in this profile, this function will return NULL
     *
     * @return Network suggestion for Passpoint
     * @see this.buildSSIDSuggestions
     * @see this.buildNetworkRequests
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
    fun buildPasspointSuggestion(): WifiNetworkSuggestion? {
        val passpointConfig = buildPasspointConfig()
        if (passpointConfig != null) {
            val suggestionBuilder = WifiNetworkSuggestion.Builder()
            suggestionBuilder.setPasspointConfig(passpointConfig)
            return suggestionBuilder.build()
        }
        return null
    }

    /**
     * Get all SSIDs for this profile
     *
     * @return SSIDs
     */
    val sSIDs: Array<String>
        get() = Arrays.copyOf(ssids, ssids.size, Array<String>::class.java)

    /**
     * Create an WifiEnterpriseConfig object which Android uses internally to configure Wi-Fi networks
     *
     * @return Wifi Enterprise configuration for this profile
     * @see this.buildPasspointConfig
     */
    protected fun buildEnterpriseConfig(): WifiEnterpriseConfig {
        val enterpriseConfig = WifiEnterpriseConfig()
        enterpriseConfig.anonymousIdentity = anonymousIdentity
        enterpriseConfig.eapMethod = enterpriseEAP
        enterpriseConfig.caCertificates = caCertificates!!.toTypedArray()
        assert(
            (serverNames.size != 0) // Checked in WifiProfile constructor
        )
        enterpriseConfig.domainSuffixMatch = serverNamesDomainString

        // Explicitly reset client certificate, will set later if needed
        enterpriseConfig.setClientKeyEntry(null, null)
        when (enterpriseEAP) {
            WifiEnterpriseConfig.Eap.TLS -> {
                // Explicitly unset unused fields
                enterpriseConfig.password = ""
                enterpriseConfig.phase2Method = WifiEnterpriseConfig.Phase2.NONE
                enterpriseConfig.setClientKeyEntry(
                    clientCertificate!!.key, clientCertificate!!.value[0]
                )

                // For TLS, "identity" is used for outer identity,
                // while for PEAP/TTLS, "identity" is the inner identity,
                // and anonymousIdentity is the outer identity
                // - so we have to do some weird shuffling here.
                enterpriseConfig.identity = anonymousIdentity
            }
            WifiEnterpriseConfig.Eap.PEAP, WifiEnterpriseConfig.Eap.TTLS, WifiEnterpriseConfig.Eap.PWD -> {
                enterpriseConfig.identity = username
                enterpriseConfig.password = password
                enterpriseConfig.phase2Method = enterprisePhase2Auth
            }
            else -> throw IllegalArgumentException("Invalid EAP type $enterpriseEAP")
        }
        return enterpriseConfig
    }

    /**
     * Get the string that Android uses for server name validation.
     *
     * Server names are treated as suffix, but exact string match is also accepted.
     *
     * On Android 9, only a single name is supported.
     * Thus, for Android 9, we will calculate the longest suffix match.
     *
     * On Android 10 and onwards, the string can be semicolon-separated,
     * which is what we will do for these platforms.
     *
     * @return The server name
     */
    private val serverNamesDomainString: String?
        private get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                java.lang.String.join(";", *serverNames)
            } else {
                getLongestSuffix(serverNames)
            }
        }

    /**
     * Create the configuration necessary to configure a passpoint and returns it
     *
     * @return Passpoint configuration for this profile
     * @see this.buildEnterpriseConfig
     */
    fun buildPasspointConfig(): PasspointConfiguration? {
        if (oids.size == 0) {
            Log.i(javaClass.simpleName, "Not creating Passpoint configuration due to no OIDs set")
            return null
        }
        val passpointConfig = PasspointConfiguration()
        val homeSp = HomeSp()
        // The FQDN in this case is the server names being used to verify the server certificate
        // Passpoint also has a domain, which is set later with Credential.setRealm(fqdn)
        homeSp.fqdn = serverNamesDomainString
        homeSp.friendlyName = "$fqdn via Passpoint"
        homeSp.roamingConsortiumOis = oids
        passpointConfig.homeSp = homeSp
        val cred = Credential()
        val rootCertificates =
            caCertificates!!.stream().filter(object : Predicate<X509Certificate> {
                override fun test(certificate: X509Certificate): Boolean {
                    return isRootCertificate(certificate)
                }
            }).collect(Collectors.toList())
        // TODO Add support for multiple CAs
        if (rootCertificates.size == 1) {
            // Just use the first CA for Passpoint
            cred.caCertificate = rootCertificates.get(0)
        } else {
            Log.e(
                javaClass.simpleName,
                "Not creating Passpoint configuration due to too many CAs in the profile (1 supported, $rootCertificates given)"
            )
            return null
        }
        cred.realm = fqdn
        when (enterpriseEAP) {
            WifiEnterpriseConfig.Eap.TLS -> {
                val certCred = CertificateCredential()
                certCred.certType = "x509v3"
                cred.clientPrivateKey = clientCertificate!!.key
                cred.clientCertificateChain = clientCertificate!!.value
                certCred.certSha256Fingerprint = getFingerprint(
                    clientCertificate!!.value.get(0)
                )
                cred.certCredential = certCred
            }
            WifiEnterpriseConfig.Eap.PWD -> {
                Log.i(
                    javaClass.simpleName,
                    "Not creating Passpoint configuration due to unsupported EAP type $enterpriseEAP"
                )
                return null // known but unsupported EAP method
            }
            WifiEnterpriseConfig.Eap.PEAP, WifiEnterpriseConfig.Eap.TTLS -> {
                val passwordBytes =
                    password!!.toByteArray(Charset.defaultCharset()) // TODO explicitly use UTF-8?
                val base64 = Base64.encodeToString(passwordBytes, Base64.DEFAULT)
                val us = UserCredential()
                us.username = username
                us.password = base64
                us.eapType = 21 // 21 indicates TTLS (RFC 5281)
                when (enterprisePhase2Auth) {
                    WifiEnterpriseConfig.Phase2.MSCHAPV2 -> us.nonEapInnerMethod = "MS-CHAP-V2"
                    WifiEnterpriseConfig.Phase2.PAP -> us.nonEapInnerMethod = "PAP"
                    WifiEnterpriseConfig.Phase2.MSCHAP -> us.nonEapInnerMethod = "MS-CHAP"
                    else -> throw IllegalArgumentException("Invalid Phase2 type $enterprisePhase2Auth")
                }
                cred.userCredential = us
            }
            else -> throw IllegalArgumentException("Invalid EAP type $enterpriseEAP")
        }
        passpointConfig.credential = cred
        return passpointConfig
    }

    /**
     * Create Network Requests which can be used to configure a network on API 30 and up
     *
     * The advantage of Network Requests is that they are visible as real networks,
     * as opposed to Suggestions, which are only visible when connected.
     *
     * @return List of network requests
     * @see this.buildSSIDSuggestions
     * @see this.buildPasspointSuggestion
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    fun buildNetworkRequests(): List<NetworkRequest> {
        val enterpriseConfig = buildEnterpriseConfig()
        val networkRequestBuilder = NetworkRequest.Builder()
        networkRequestBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        return Arrays.stream(ssids).map { ssid ->
            val builder = WifiNetworkSpecifier.Builder()
            builder.setSsid((ssid)!!)
            builder.setWpa2EnterpriseConfig(enterpriseConfig)
            val wifiNetworkSpecifier = builder.build()
            networkRequestBuilder.setNetworkSpecifier(wifiNetworkSpecifier)
            networkRequestBuilder.build()
        }.collect(Collectors.toList())
        // TODO create Passpoint network request
    }

    /**
     * Create a WifiConfiguration object which can be installed on API target 28
     *
     * @return List of Wi-Fi configurations
     */
    fun buildWifiConfigurations(): List<WifiConfiguration> {
        val enterpriseConfig = buildEnterpriseConfig()
        return ssids.map { ssid ->
            val config = WifiConfiguration()
            config.SSID = "\"" + ssid + "\""
            config.priority = 1
            config.status = WifiConfiguration.Status.ENABLED
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP)
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X)
            config.enterpriseConfig = enterpriseConfig
            config
        }
    }

    companion object {
        /**
         * Check if the passphrase can decrypt the PKCS12 container
         *
         * @param clientCertificate Base64 encoded client certificate in a PKCS12 container
         * @param passphrase        The passphrase
         * @return The passphrase can decrypt the client certificate
         * @throws NullPointerException  Any of the parameters was NULL
         * @throws IllegalStateException Internal error
         */
        @JvmStatic
        @Throws(NullPointerException::class, IllegalStateException::class)
        fun validatePassPhrase(clientCertificate: String?, passphrase: String): Boolean {
            try {
                val pkcs12ks = KeyStore.getInstance("pkcs12")
                val bytes = Base64.decode(clientCertificate, Base64.NO_WRAP)
                val b = ByteArrayInputStream(bytes)
                val `in`: InputStream = BufferedInputStream(b)
                pkcs12ks.load(`in`, passphrase.toCharArray())
            } catch (e: CertificateException) {
                return false
            } catch (e: IOException) {
                throw IllegalStateException("The passphrase could not be tested", e)
            } catch (e: NoSuchAlgorithmException) {
                throw IllegalStateException("The passphrase could not be tested", e)
            } catch (e: KeyStoreException) {
                throw IllegalStateException("The passphrase could not be tested", e)
            }
            return true
        }

        /**
         * Convert from `Long[]` to `long[]`
         */
        private fun toLongPrimitive(objects: Array<Long?>): LongArray {
            val primitives = LongArray(objects.size)
            for (i in objects.indices) primitives[i] = objects[i]
            return primitives
        }

        /**
         * Convert a JSONArray of strings to a native String array
         *
         * @param array Input array
         * @return Output array
         * @throws JSONException An entry in the input array contains something different than a String
         */
        @Throws(JSONException::class)
        private fun jsonArrayToStringArray(array: JSONArray): Array<String?> {
            val result = arrayOfNulls<String>(array.length())
            for (i in 0 until array.length()) result[i] = array.getString(i)
            return result
        }

        /**
         * Converts an IANA EAP type integer to an Android EAP type integer
         *
         * @param ianaEAPMethod EAP type as used in eap-config
         * @return A value from WifiEnterpriseConfig.Eap (TLS,TTLS,PEAP)
         * @throws EapConfigValueException If there is no mapping from the ianaEAPMethod to an Android EAP type
         * @link https://www.iana.org/assignments/eap-numbers/eap-numbers.xhtml#eap-numbers-4
         */
        @Throws(EapConfigValueException::class)
        private fun getAndroidEAPTypeFromIanaEAPType(ianaEAPMethod: Int): Int {
            when (ianaEAPMethod) {
                13 -> return WifiEnterpriseConfig.Eap.TLS
                21 -> return WifiEnterpriseConfig.Eap.TTLS
                25 -> return WifiEnterpriseConfig.Eap.PEAP
                else -> throw EapConfigValueException("Unknown IANA EAP type $ianaEAPMethod")
            }
        }

        /**
         * Converts a eap-config/CAT inner type to an Android Phase2 integer
         *
         * @param eapConfigAuthMethod Auth method as used in eap-config
         * @return ENUM from WifiEnterpriseConfig.Phase2 (PAP/MSCHAP/MSCHAPv2) or -1 if no match
         * @throws EapConfigValueException If there is no mapping from the eapConfigAuthMethod to an Android Phase 2 integer
         */
        @Throws(EapConfigValueException::class)
        private fun getAndroidPhase2FromCATAuthMethod(eapConfigAuthMethod: Int): Int {
            when (eapConfigAuthMethod) {
                -1 -> return WifiEnterpriseConfig.Phase2.PAP
                -2 -> return WifiEnterpriseConfig.Phase2.MSCHAP
                -3, 26 ->                // This currently DOES happen because CAT has a bug where it reports TTLS-MSCHAPv2 as TTLS-EAP-MSCHAPv2,
                    // so denying this would prevent profiles from being side-loaded
                    return WifiEnterpriseConfig.Phase2.MSCHAPV2
                else -> throw EapConfigValueException("Unknown eap-config auth method $eapConfigAuthMethod")
            }
        }

        /**
         * Get the longest common suffix domain components from a list of hostnames
         *
         * @param strings A list of host names
         * @return The longest common suffix for all given host names
         */
        fun getLongestSuffix(strings: Array<String?>): String? {
            if (strings.size == 0) return ""
            if (strings.size == 1) return strings[0]
            var longest = strings[0]
            for (candidate: String? in strings) {
                var pos = candidate!!.length
                do {
                    pos = candidate.lastIndexOf('.', pos - 2) + 1
                } while (pos > 0 && longest!!.endsWith(candidate.substring(pos)))
                if (!longest!!.endsWith(candidate.substring(pos))) {
                    pos = candidate.indexOf('.', pos)
                }
                if (pos == -1) {
                    longest = ""
                } else if (longest.endsWith(candidate.substring(pos))) {
                    longest = candidate.substring(if (pos == 0) 0 else pos + 1)
                }
            }
            return longest
        }

        /**
         * Returns fingerprint of a certificate
         *
         * @param certificate The certificate to inspect
         * @return The fingerprint of the certificate
         */
        private fun getFingerprint(certificate: X509Certificate): ByteArray {
            try {
                val digester = MessageDigest.getInstance("SHA-256")
                digester.reset()
                return digester.digest(certificate.encoded)
            } catch (e: NoSuchAlgorithmException) {
                throw IllegalStateException("Unable to digest SHA-256", e)
            } catch (e: CertificateEncodingException) {
                throw IllegalArgumentException("Unable to encode certificate as DER", e)
            }
        }

        /**
         * Extract private key and certificate chain from a PKCS12 store
         *
         * @param pkcs12StoreB64 PKCS12 store base64 encoded
         * @param passphrase     Passphrase to open the PKCS12 store
         * @return Tuple with private key and certificate + chain
         * @throws NullPointerException      NULL PKCS12 store provided
         * @throws CertificateException      Certificate from the store could not be loaded
         * @throws NoSuchAlgorithmException  Algorithm for checking integrity or recovering the private key cannot be found
         * @throws UnrecoverableKeyException Key cannot be recovered; typically incorrect passphrase
         */
        @Throws(
            CertificateException::class,
            NoSuchAlgorithmException::class,
            UnrecoverableKeyException::class
        )
        private fun getClientCertificate(
            pkcs12StoreB64: String, passphrase: String?
        ): Map.Entry<PrivateKey, Array<X509Certificate>> {
            try {
                val bytes = Base64.decode(pkcs12StoreB64, Base64.NO_WRAP)
                val passphraseBytes = passphrase?.toCharArray() ?: CharArray(0)
                val pkcs12ks = KeyStore.getInstance("pkcs12")
                val b = ByteArrayInputStream(bytes)
                val `in`: InputStream = BufferedInputStream(b)
                pkcs12ks.load(`in`, passphraseBytes)
                val aliases = pkcs12ks.aliases()
                while (aliases.hasMoreElements()) {
                    val alias = aliases.nextElement()
                    val chain = pkcs12ks.getCertificateChain(alias)
                    if (chain != null && chain.size > 0) try {
                        return AbstractMap.SimpleEntry(
                            pkcs12ks.getKey(alias, passphraseBytes) as PrivateKey,
                            Arrays.copyOf(chain, chain.size, Array<X509Certificate>::class.java)
                        )
                    } catch (e: ArrayStoreException) {
                        Log.w(
                            "WifiProfile",
                            "A certificate in the ClientCertificate chain is not an instance of X509Certificate"
                        )
                    }
                }
                // KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException
            } catch (e: KeyStoreException) {
                throw IllegalArgumentException("Unable to read ", e)
            } catch (e: IOException) {
                if (e.cause is UnrecoverableKeyException) throw (e.cause as UnrecoverableKeyException?)!!
                throw IllegalStateException("Unexpected I/O error reading key data")
            }
            throw IllegalArgumentException("Cannot extract a X509Certificate from the certificate store")
        }

        /**
         * Convert an array of base64 encoded DER certificates to X509Certificate objects
         *
         * @param caCertificates DER+Base64 encoded X509 certificates
         * @return Native X509Certificate objects
         * @throws CertificateException Unable to parse a certificate
         */
        @Throws(CertificateException::class)
        private fun getCertificates(caCertificates: Array<String?>): Array<X509Certificate?> {
            var certFactory: CertificateFactory
            val certificates = arrayOfNulls<X509Certificate>(caCertificates.size)
            // building the certificates
            for (i in caCertificates.indices) {
                val bytes = Base64.decode(caCertificates[i], Base64.NO_WRAP)
                val b = ByteArrayInputStream(bytes)
                certFactory = CertificateFactory.getInstance("X.509")
                val certificate = certFactory.generateCertificate(b) as X509Certificate
                certificates[i] = certificate
            }
            return certificates
        }

        /**
         * Check that a certificate is marked as a CA
         *
         * A qualifying certificate has either KeyUsage bit 5 set,
         * or has the first byte in OID 2.5.29.19 set to non-zero (not boolean false)
         *
         * @param certificate The certificate to check
         * @return The certificate is a CA certificate
         */
        private fun isCA(certificate: X509Certificate): Boolean {

            val usage = certificate.keyUsage

            // https://docs.oracle.com/javase/7/docs/api/java/security/cert/X509Certificate.html#getKeyUsage()
            // 5 is KeyUsage keyCertSign, which indicates the certificate is a CA
            if ((usage != null) && (usage.size > 5) && usage[5]) {
                // This is a CA according to KeyUsage
                return true
            } else {
                // Find out if this a CA according to Basic Constraints
                val extension = certificate.getExtensionValue("2.5.29.19")
                return (extension != null) && (extension.size > 1) && (extension[0].toInt() != 0)
            }
        }

        /**
         * Determines whether a certificate is a root certificate
         *
         * A root certificate is defined by being self-signed (issuer == subject) and being recognised
         * as a CA by `isCA`.
         *
         * @param certificate The certificate to test
         * @return The certificate is a root certificate
         */
        protected fun isRootCertificate(certificate: X509Certificate): Boolean {
            return (certificate.subjectDN.toString() == certificate.issuerDN.toString()) && isCA(
                certificate
            )
        }
    }
}