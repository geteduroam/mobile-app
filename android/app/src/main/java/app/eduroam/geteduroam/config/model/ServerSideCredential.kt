package app.eduroam.geteduroam.config.model

import kotlinx.serialization.Serializable
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root
@Serializable
class ServerSideCredential {

    @field:ElementList(entry = "ServerID", inline = true, required = false)
    var serverId: List<String>? = null

    @field:ElementList(name = "CA", inline = true, required = false)
    var certData: List<CertData>? = null

}