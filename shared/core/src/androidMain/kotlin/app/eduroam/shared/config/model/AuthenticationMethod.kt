package app.eduroam.shared.config.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "AuthenticationMethod")
class AuthenticationMethod {

    @field:Element(name = "EAPMethod", required = false)
    var eapMethod: EAPMethod? = null

    @field:Element(name = "ServerSideCredential", required = false)
    var serverSideCredential: ServerSideCredential? = null

    @field:Element(name = "ClientSideCredential", required = false)
    var clientSideCredential: ClientSideCredential? = null

    @field:Element(name = "InnerAuthenticationMethod", required = false)
    var innerAuthenticationMethod: InnerAuthenticationMethod? = null

}