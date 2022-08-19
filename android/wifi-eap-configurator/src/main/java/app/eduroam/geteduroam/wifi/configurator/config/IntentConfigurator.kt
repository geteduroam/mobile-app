package app.eduroam.geteduroam.wifi.configurator.config

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresApi

/**
 * A configurator using intents
 *
 * Intents will create the network in the WiFi app just like other WiFi networks.
 * This is the most natural behaviour for the end user.
 * If a network is created that already exists, the network is overridden (*)
 *
 * It does not appear to be possible to remove networks after they have been created.
 * This is especially problematic for Passpoint networks;
 *
 * (*) on some devices, Passpoint networks are not overridden
 */
@RequiresApi(api = Build.VERSION_CODES.R)
class IntentConfigurator(context: Context?) : SuggestionConfigurator(context) {
    override fun installSuggestions(suggestions: List<WifiNetworkSuggestion>) {
        // TODO ideally we want to remove old networks, especially Passpoint
        var suggestions: List<WifiNetworkSuggestion>? = suggestions
        if (suggestions !is ArrayList<*>) {
            suggestions = ArrayList(suggestions)
        }
        val bundle = Bundle()
        bundle.putParcelableArrayList(
            Settings.EXTRA_WIFI_NETWORK_LIST,
            suggestions as ArrayList<WifiNetworkSuggestion>?
        )
        val intent = Intent(Settings.ACTION_WIFI_ADD_NETWORKS)
        intent.putExtras(bundle)
        (context as Activity).startActivityForResult(intent, ADD_NETWORKS_REQUEST_CODE)
    }

    /**
     * Remove networks with matching SSIDs
     *
     * @param ssids Remove network matching these SSIDs
     */
    override fun removeNetwork(vararg ssids: String) {
        // It's not possible to remove SSIDs that are created through intentions
        // But let's remove everything to be sure
        removeNetwork()
    }

    /**
     * Checks if a network with the given SSID is configured
     *
     * @param ssid Check if a network with this SSID exists
     * @return A network with the given SSID exists
     */
    override fun isNetworkConfigured(ssid: String?): Boolean {
        return false
    }

    /**
     * Checks if the network with the given SSID can be overridden
     *
     * @param ssid Check if a network with this SSID can be overridden
     * @return The network with the given SSID can be overridden
     */
    override fun isNetworkOverrideable(ssid: String?): Boolean {
        return false
    }

    companion object {
        const val ADD_NETWORKS_REQUEST_CODE = 100
    }
}