package app.eduroam.geteduroam.config

import android.util.Base64
import android.util.Log
import app.eduroam.geteduroam.config.model.ClientSideCredential
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.security.*
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*

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
fun ClientSideCredential.getClientCertificate(
): Map.Entry<PrivateKey, Array<X509Certificate>> {
    try {
        val bytes = Base64.decode(clientCertificate?.value, Base64.NO_WRAP)
        val passphraseBytes = passphrase?.toCharArray() ?: CharArray(0)
        val pkcs12ks = KeyStore.getInstance("pkcs12")
        val b = ByteArrayInputStream(bytes)
        val `in`: InputStream = BufferedInputStream(b)
        pkcs12ks.load(`in`, passphraseBytes)
        val aliases = pkcs12ks.aliases()
        while (aliases.hasMoreElements()) {
            val alias = aliases.nextElement()
            val chain = pkcs12ks.getCertificateChain(alias)
            if (chain != null && chain.isNotEmpty()) try {
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
