package app.eduroam.shared.config.model

import org.simpleframework.xml.Element

class ClientSideCredential {

    @field:Element(name = "InnerIdentitySuffix", required = false)
    var innerIdentitySuffix: String? = null

    @field:Element(name = "InnerIdentityPrefix", required = false)
    var innerIdentityPrefix: String? = null

    @field:Element(name = "OuterIdentity", required = false)
    var outerIdentity: String? = null

    @field:Element(name = "UserName", required = false)
    var userName: String? = null

    @field:Element(name = "Password", required = false)
    var password: String? = null

    @field:Element(name = "Passphrase", required = false)
    var passphrase: String? = null

    @field:Element(name = "ClientCertificate", required = false)
    var clientCertificate: CertData? = null

}