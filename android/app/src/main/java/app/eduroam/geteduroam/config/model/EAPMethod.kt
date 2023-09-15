package app.eduroam.geteduroam.config.model

import com.squareup.moshi.JsonClass
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "EAPMethod")
@JsonClass(generateAdapter = true)
class EAPMethod {

    @field:Element(name = "Type", required = false)
    var type: String? = null

}