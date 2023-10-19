package app.eduroam.geteduroam.config.model

import com.squareup.moshi.JsonClass
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root
@JsonClass(generateAdapter = true)
class ServerSideCredential {

    @field:Element(name = "ServerID", required = false)
    var serverId: String? = null

    @field:ElementList(name = "CA", inline = true, required = false)
    var certData: List<CertData>? = null

}