package app.eduroam.geteduroam.config.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root
class ServerSideCredential {

    @field:Element(name = "ServerID", required = false)
    var serverId: String? = null

    @field:ElementList(inline = true)
    var cartData: List<CertData>? = null

}