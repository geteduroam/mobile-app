package app.eduroam.shared.config

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
)

//Client certificate passed as a base64 encoded string and decoded for each platform into platform specific certificate type.
data class ClientCertificate(val privateKeyBase64: String, val certificate: String)