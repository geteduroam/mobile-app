package app.eduroam.geteduroam.config.model

import kotlinx.serialization.Serializable
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "InnerAuthenticationMethod")
@Serializable
class InnerAuthenticationMethod {

    @field:Element(name = "NonEAPAuthMethod", required = false)
    var nonEapMethod: NonEAPAuthMethod? = null

    @field:Element(name = "EAPMethod", required = false)
    var eapMethod: EAPMethod? = null

}