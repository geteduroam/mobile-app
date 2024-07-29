package app.eduroam.geteduroam.config.model

import kotlinx.serialization.Serializable
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "EAPIdentityProviderList", strict = false)
@Serializable
data class EAPIdentityProviderList(
    @field:ElementList(name = "EAPIdentityProvider", inline = true)
    var eapIdentityProvider: List<EAPIdentityProvider>? = null
)


