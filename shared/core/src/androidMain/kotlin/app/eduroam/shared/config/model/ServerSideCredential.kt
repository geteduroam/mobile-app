package app.eduroam.shared.config.model

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root
import org.simpleframework.xml.Text

@Root
class ServerSideCredential {

    @field:Element(name = "ServerID", required = false)
    var serverId: String? = null

    @field:Element(name = "CA", required = false)
    var cartData: CertData? = null

}