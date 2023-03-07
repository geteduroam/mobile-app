package app.eduroam.shared.config.model

import org.simpleframework.xml.Element

class Helpdesk {

    @field:Element(name = "EmailAddress", required = false)
    var emailAddress: String? = null

    @field:Element(name = "WebAddress", required = false)
    var webAddress: String? = null

    @field:Element(name = "Phone", required = false)
    var phone: String? = null
}