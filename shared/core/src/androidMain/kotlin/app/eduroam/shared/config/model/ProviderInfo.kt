package app.eduroam.shared.config.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "ProviderInfo")
class ProviderInfo {

    @field:Element(name = "DisplayName")
    var displayName: String? = null

    @field:Element(name = "Description")
    var description: String? = null

    @field:Element(name = "ProviderLocation")
    var providerLocation: ProviderLocation? = null

    @field:Element(name = "ProviderLogo")
    var providerLogo: ProviderLogo? = null

    @field:Element(name = "TermsOfUse")
    var termsOfUse: String? = null

    @field:Element(name = "Helpdesk")
    var helpdesk: Helpdesk? = null

}