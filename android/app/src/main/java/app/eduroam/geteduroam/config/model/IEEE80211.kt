package app.eduroam.geteduroam.config.model

import kotlinx.serialization.Serializable
import org.simpleframework.xml.Element

@Serializable
class IEEE80211 {

    @field:Element(name = "SSID", required = false)
    var ssid: String? = null

    @field:Element(name = "ConsortiumOID", required = false)
    var consortiumOID: String? = null

    @field:Element(name = "MinRSNProto", required = false)
    var minRSNProto: String? = null
}