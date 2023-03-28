package app.eduroam.shared.config.model

import kotlinx.serialization.Serializable
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Serializable
@Root(name = "EAPIdentityProviderList", strict = false)
class EAPIdentityProviderList {

    @field:ElementList(name = "EAPIdentityProvider", inline = true)
    var eapIdentityProvider: List<EAPIdentityProvider>? = null

}


