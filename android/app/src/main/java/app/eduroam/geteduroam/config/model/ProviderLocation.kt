package app.eduroam.geteduroam.config.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root
class ProviderLocation {

    @field:Element(name = "Longitude", required = false)
    var longitude: String? = null

    @field:Element(name = "Latitude", required = false)
    var latitude: String? = null
}