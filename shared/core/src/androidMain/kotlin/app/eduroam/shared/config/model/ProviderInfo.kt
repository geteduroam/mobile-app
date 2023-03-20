package app.eduroam.shared.config.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "ProviderInfo")
class ProviderInfo {

    @field:Element(name = "DisplayName", required = false)
    var displayName: String? = null

    @field:Element(name = "Description", required = false)
    var description: String? = null

    @field:Element(name = "ProviderLocation", required = false)
    var providerLocation: ProviderLocation? = null

    @field:Element(name = "ProviderLogo", required = false)
    var providerLogo: ProviderLogo? = null

    @field:Element(name = "TermsOfUse", required = false)
    var termsOfUse: String? = null

    @field:Element(name = "Helpdesk", required = false)
    var helpdesk: Helpdesk? = null

}