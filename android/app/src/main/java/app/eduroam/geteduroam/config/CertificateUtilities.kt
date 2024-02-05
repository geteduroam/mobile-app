package app.eduroam.geteduroam.config

import android.util.Base64
import android.util.Log
import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateEncodingException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate


fun isRootCertificate(certificate: X509Certificate): Boolean =
    (certificate.subjectDN.toString() == certificate.issuerDN.toString()) && isCA(
        certificate
    )

/**
 * Check that a certificate is marked as a CA
 *
 * A qualifying certificate has either KeyUsage bit 5 set,
 * or has the first byte in OID 2.5.29.19 set to non-zero (not boolean false)
 *
 * @param certificate The certificate to check
 * @return The certificate is a CA certificate
 */
fun isCA(certificate: X509Certificate): Boolean {

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

@Throws(CertificateException::class)
fun getCertificates(caCertificates: List<String>?): List<X509Certificate> =
    caCertificates?.mapNotNull { certificate ->
        try {
            val certFactory = CertificateFactory.getInstance("X.509")
            val byteArrayInputStream =
                ByteArrayInputStream(Base64.decode(certificate, Base64.NO_WRAP))
            certFactory.generateCertificate(byteArrayInputStream) as X509Certificate
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse certificate", e)
            null
        }
    } ?: emptyList()


/**
 * Returns fingerprint of a certificate
 *
 * @param certificate The certificate to inspect
 * @return The fingerprint of the certificate
 */
fun getFingerprint(certificate: X509Certificate): ByteArray {
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

private const val TAG = "CertificateUtilities"