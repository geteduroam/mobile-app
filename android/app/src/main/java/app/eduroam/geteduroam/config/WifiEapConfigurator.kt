package app.eduroam.geteduroam.config

import android.net.wifi.WifiNetworkSuggestion
import app.eduroam.shared.config.WifiConfigData

class WifiEapConfigurator {

    /**
     * Creates the WifiNetworkSuggestion list to be used in the configuration intent.
     * The PasspointConfiguration is only supported starting from Android 11 (API 30) and higher
     * */
    fun createWifiNetworkSuggestionList(
        profile: WifiConfigData, includePasspoint: Boolean = false
    ): List<WifiNetworkSuggestion> {
        val suggestions = profile.buildSSIDSuggestions()
        val passpointSuggestions = profile.buildPasspointSuggestion()
        return if (passpointSuggestions != null && includePasspoint) {
            suggestions + passpointSuggestions
        } else {
            suggestions
        }
    }
}