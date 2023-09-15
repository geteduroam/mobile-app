package app.eduroam.geteduroam.config.model

import com.squareup.moshi.JsonClass
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Text

@JsonClass(generateAdapter = true)
class ProviderLogo {

    @field:Text
    var value: String? = null

    @field:Attribute
    var mime: String? = null

    @field:Attribute
    var encoding: String? = null
}