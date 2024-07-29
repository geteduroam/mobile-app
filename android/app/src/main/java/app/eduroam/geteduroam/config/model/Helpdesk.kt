package app.eduroam.geteduroam.config.model

import kotlinx.serialization.Serializable
import org.simpleframework.xml.Element

@Serializable
class Helpdesk {

    @field:Element(name = "EmailAddress", required = false)
    var emailAddress: String? = null

    @field:Element(name = "WebAddress", required = false)
    var webAddress: String? = null

    @field:Element(name = "Phone", required = false)
    var phone: String? = null
}