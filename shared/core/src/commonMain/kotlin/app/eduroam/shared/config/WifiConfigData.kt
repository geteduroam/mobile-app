package app.eduroam.shared.config

import kotlinx.serialization.Serializable

@Serializable
data class WifiConfigData(
    val ssids: List<String?>,
    val oids: List<Long>,
    var clientCertificate: ClientCertificate? = null,
    val anonymousIdentity: String? = null,
    //Working with certificate as base64 encoded strings, to be parsed by the platform into platform specific type.
    val caCertificates: List<String>? = null,
    val enterpriseEAP: Int = 0,
    val serverNames: List<String?>? = null,
    val username: String? = null,
    val password: String? = null,
    val enterprisePhase2Auth: Int = 0,
    val fqdn: String? = null,
) {
    /**
     * Get the longest common suffix domain components from a list of hostnames
     *
     * @param strings A list of host names
     * @return The longest common suffix for all given host names
     */
    fun getLongestSuffix(strings: List<String?>?): String? {
        if (strings.isNullOrEmpty()) return ""
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
}

//Client certificate passed as a base64 encoded string and decoded for each platform into platform specific certificate type.
@Serializable
data class ClientCertificate(val passphrase: String?, val pkcs12StoreB64: String)