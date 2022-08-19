package app.eduroam.geteduroam.wifi.configurator.config

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * A configurator using Network Requests
 *
 * Network Requests trigger a prompt for the user to connect to a "temporary network"
 *
 * This is not useful for configuring eduroam for long-term use,
 * but it might be useful for connecting to an onboarding network.
 *
 * Because of this, currently this class is unused, but it's kept for possible further use.
 */
@RequiresApi(api = Build.VERSION_CODES.R)
class RequestConfigurator internal constructor(context: Context) : AbstractConfigurator(context) {
    val cm = context.applicationContext
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun installNetworkRequests(vararg networkRequests: NetworkRequest?) {
        for (networkRequest in networkRequests) {
            cm.requestNetwork(networkRequest!!, object : NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    cm.bindProcessToNetwork(network)
                }
            })
        }
    }

    /**
     * Remove networks with matching SSIDs
     *
     * @param ssids Remove network matching these SSIDs
     */
    override fun removeNetwork(vararg ssids: String?) {
        // Unable to remove those?
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
}