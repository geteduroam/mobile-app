package app.eduroam.geteduroam.config.model

import com.squareup.moshi.JsonClass
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "EAPIdentityProviderList", strict = false)
@JsonClass(generateAdapter = true)
data class EAPIdentityProviderList(
    @field:ElementList(name = "EAPIdentityProvider", inline = true)
    var eapIdentityProvider: List<EAPIdentityProvider>? = null
)


