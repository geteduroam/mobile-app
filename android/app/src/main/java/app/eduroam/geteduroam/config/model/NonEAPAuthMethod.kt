package app.eduroam.geteduroam.config.model

import kotlinx.serialization.Serializable
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "NonEAPAuthMethod")
@Serializable
class NonEAPAuthMethod {

    @field:Element(name = "Type", required = false)
    var type: String? = null

}