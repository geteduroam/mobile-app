package app.eduroam.shared.config.model

import kotlinx.serialization.Serializable
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Text

@Serializable
class ProviderLogo {

    @field:Text
    var value: String? = null

    @field:Attribute
    var mime: String? = null

    @field:Attribute
    var encoding: String? = null
}