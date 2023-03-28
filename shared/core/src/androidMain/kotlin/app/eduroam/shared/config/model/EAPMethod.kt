package app.eduroam.shared.config.model

import kotlinx.serialization.Serializable
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Serializable
@Root(name = "EAPMethod")
class EAPMethod {

    @field:Element(name = "Type", required = false)
    var type: String? = null

}