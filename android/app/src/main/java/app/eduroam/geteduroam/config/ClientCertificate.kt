package app.eduroam.geteduroam.config

import kotlinx.serialization.Serializable

@Serializable
data class ClientCertificate(val passphrase: String?, val pkcs12StoreB64: String)