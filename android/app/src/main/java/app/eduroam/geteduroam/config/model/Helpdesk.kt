package app.eduroam.geteduroam.config.model

import com.squareup.moshi.JsonClass
import org.simpleframework.xml.Element

@JsonClass(generateAdapter = true)
class Helpdesk {

    @field:Element(name = "EmailAddress", required = false)
    var emailAddress: String? = null

    @field:Element(name = "WebAddress", required = false)
    var webAddress: String? = null

    @field:Element(name = "Phone", required = false)
    var phone: String? = null
}