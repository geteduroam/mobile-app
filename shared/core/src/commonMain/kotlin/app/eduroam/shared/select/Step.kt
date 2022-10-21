package app.eduroam.shared.select

import app.eduroam.shared.config.WifiConfigData
import app.eduroam.shared.response.Institution
import app.eduroam.shared.response.Profile

sealed class Step {
    object Start : Step()
    data class DoConfig(val wifiConfigData: WifiConfigData) : Step()
    data class DoOAuthFor(val profile: Profile, val authorizationUrl: String) : Step()
    data class PickProfileFrom(val institution: Institution) : Step()
}