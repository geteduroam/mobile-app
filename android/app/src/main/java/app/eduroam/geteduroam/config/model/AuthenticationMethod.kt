package app.eduroam.geteduroam.config.model

import android.net.wifi.WifiEnterpriseConfig
import app.eduroam.geteduroam.config.convertEAPMethod
import app.eduroam.geteduroam.config.getClientCertificate
import kotlinx.serialization.Serializable
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "AuthenticationMethod")
@Serializable
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

fun List<AuthenticationMethod>.bestMethod(): AuthenticationMethod? {
    return firstOrNull {
        val method = it.eapMethod?.type?.toInt()?.convertEAPMethod()
        method == WifiEnterpriseConfig.Eap.PEAP ||
                method == WifiEnterpriseConfig.Eap.TTLS ||
                method == WifiEnterpriseConfig.Eap.PWD ||
                (method == WifiEnterpriseConfig.Eap.TLS && it.clientSideCredential?.getClientCertificate()!= null)
    } ?:
    firstOrNull()
}