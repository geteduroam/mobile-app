package app.eduroam.shared.config.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "EAPMethod")
class EAPMethod {

    @field:Element(name = "Type", required = false)
    var type: String? = null

}