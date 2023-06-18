package app.eduroam.geteduroam.config.model

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Root
import org.simpleframework.xml.Text

@Root(name = "CA")
class CertData {

    @field:Text
    var value: String? = null

    @field:Attribute
    var format: String? = null

    @field:Attribute
    var encoding: String? = null
}