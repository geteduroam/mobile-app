package app.eduroam.shared.config.model

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root


@Root(name = "EAPIdentityProviderList", strict = false)
class EAPIdentityProviderList {

    @field:ElementList(name = "EAPIdentityProvider", inline = true)
    var eapIdentityProvider: List<EAPIdentityProvider>? = null

}


