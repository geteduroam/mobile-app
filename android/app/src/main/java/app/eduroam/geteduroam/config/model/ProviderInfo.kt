package app.eduroam.geteduroam.config.model

import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "ProviderInfo")
@JsonClass(generateAdapter = true)
@Serializable
data class ProviderInfo(
    @field:Element(name = "DisplayName", required = false)
    var displayName: String? = null,

    @field:Element(name = "Description", required = false)
    var description: String? = null,

    @field:ElementList(inline = true, entry = "ProviderLocation", required = false)
    var providerLocations: List<ProviderLocation>? = null,

    @field:Element(name = "ProviderLogo", required = false)
    var providerLogo: ProviderLogo? = null,

    @field:Element(name = "TermsOfUse", required = false)
    var termsOfUse: String? = null,

    @field:Element(name = "Helpdesk", required = false)
    var helpdesk: Helpdesk? = null
)