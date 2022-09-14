package app.eduroam.geteduroam.wifi.configurator.config

import android.content.Context
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import app.eduroam.geteduroam.wifi.configurator.exception.NetworkSuggestionException

/**
 * An implementation for the configurator that uses Suggestions.
 *
 * Suggestions appear either as a background notification (Android 10) or as a prompt (Android 11).
 * After the user answers the prompt positively, the device will connect to the network if it is
 * in range, and no competing user WiFi networks are available.  The networks from the suggestion
 * do not show up in the WiFi manager, and an attempt from the user to connect to the network
 * manually might trigger a dialog asking for settings, even though the suggestion is already
 * installed.
 *
 * On Android 10 (but not 11), there might be a cooldown timer that is triggered when the user
 * answers negatively to the prompt.  The timer reportedly runs for 24 hours, cannot be cancelled
 * and prevents further prompts from showing up.
 *
 * This method is used as a fallback on Android 11, but on Android 10 it is the only supported
 * means of configuring WiFi, despite the issues.
 */
@RequiresApi(api = Build.VERSION_CODES.Q)
open class SuggestionConfigurator(context: Context?) : AbstractConfigurator(
    context!!
) {
    @Throws(NetworkSuggestionException::class)
    open fun installSuggestions(suggestions: List<WifiNetworkSuggestion?>?) {
        //if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {

        // From Android 11, suggestions may be duplicate
        // but on Android 10, we must remove suggestions first.
        // On Android 10, we cannot get our previously configured suggestions,
        // so we have to use the only other option; remove all our installed suggestions.

        // On Android 11 we will also remove all networks, because otherwise old suggestions
        // will keep lingering.

        // TODO If we're ever going to support multiple profiles, we must store which suggestions are connected to the profile,
        // so that we only remove the suggestions associated with that profile
        removeNetwork()

        //}
        val status = wifiManager.addNetworkSuggestions(suggestions!!)
        if (status != 0) {
            Log.d("addNetworkSuggestions", "status: $status")
            throw NetworkSuggestionException(status)
        }
    }

    /**
     * Remove networks with matching SSIDs
     *
     * @param ssids Remove network matching these SSIDs
     */
    override fun removeNetwork(vararg ssids: String?) {
        if (ssids.isNotEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val suggestions = wifiManager.networkSuggestions
            for (suggestion in suggestions) {
                for (ssid in ssids) {
                    if (ssid == suggestion.ssid) break
                }
                suggestions.remove(suggestion)
            }
            wifiManager.removeNetworkSuggestions(suggestions)
        } else {
            removeNetwork()
        }
    }

    /**
     * Remove all our configured networks
     */
    fun removeNetwork() {
        // Empty list removes all networks
        wifiManager.removeNetworkSuggestions(emptyList())
    }

    /**
     * Checks if a network with the given SSID is configured
     *
     * @param ssid Check if a network with this SSID exists
     * @return A network with the given SSID exists
     */
    override fun isNetworkConfigured(ssid: String?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val installedSuggestions = wifiManager.networkSuggestions
            for (suggestion in installedSuggestions) {
                if (ssid == suggestion.ssid) return true
            }
        }

        // We're not sure it's not configured, but it's not configured by us
        return false
    }

    /**
     * Checks if the network with the given SSID can be overridden
     *
     * @param ssid Check if a network with this SSID can be overridden
     * @return The network with the given SSID can be overridden
     */
    override fun isNetworkOverrideable(ssid: String?): Boolean {
        // We don't know, so go ahead!
        return true
    }
}