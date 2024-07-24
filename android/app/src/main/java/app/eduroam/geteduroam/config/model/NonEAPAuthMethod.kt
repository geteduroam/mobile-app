package app.eduroam.geteduroam.config.model

import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "NonEAPAuthMethod")
@JsonClass(generateAdapter = true)
@Serializable
class NonEAPAuthMethod {

    @field:Element(name = "Type", required = false)
    var type: String? = null

}