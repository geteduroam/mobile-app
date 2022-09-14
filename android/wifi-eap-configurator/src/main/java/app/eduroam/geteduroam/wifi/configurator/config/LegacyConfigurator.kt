package app.eduroam.geteduroam.wifi.configurator.config

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.hotspot2.PasspointConfiguration
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import app.eduroam.geteduroam.wifi.configurator.exception.NetworkConfigurationException
import app.eduroam.geteduroam.wifi.configurator.exception.NetworkInterfaceException
import java.util.*

/**
 * The legacy configurator will create the network in the WiFi app just like other WiFi networks.
 * This is the most natural behaviour for the end user.
 *
 * When creating a network that already exists, it is overridden.
 *
 * The LegacyConfigurator can be used to configure Wi-Fi network profiles when the target API
 * version is 28 or lower.  It works on Android 10,11, but only with a low enough target API.
 *
 * Google does not allow a low enough target API on the Google Play Store, making this class
 * impossible to use on the Play Store for Android devices with Android 10 and up.
 *
 * Not tested on Android 12.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
@TargetApi(Build.VERSION_CODES.P)
class LegacyConfigurator(context: Context?) : AbstractConfigurator(
    context!!
) {
    /**
     * Configure a network profile for devices with API 28 or lower.
     *
     * @param config The Wi-Fi configuration
     * @return ID of the network description created
     * @throws SecurityException             When adding the network was disallowed
     * @throws NetworkConfigurationException The network connection was not created
     */
    @Throws(SecurityException::class, NetworkConfigurationException::class)
    fun configureNetworkConfiguration(config: WifiConfiguration): Int {
        // Can throw SecurityException
        val networkId = wifiManager.addNetwork(config)
        if (networkId == -1) throw NetworkConfigurationException("Network " + config.SSID + " was not created. Did it already exist?")
        return networkId
    }

    /**
     * Connect to the network with the given ID, returned by configureNetworkConfiguration
     *
     * @param networkId The network ID to connect to
     */
    fun connectNetwork(networkId: Int) {
        wifiManager.disconnect()
        wifiManager.enableNetwork(networkId, true)
        wifiManager.reconnect()
        wifiManager.isWifiEnabled = true
    }

    /**
     * Remove networks with matching SSIDs
     *
     * @param ssids Remove network matching these SSIDs
     * @throws NetworkConfigurationException A network was not removed (does not throw if the network was not configured to begin with)
     */
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @Throws(
        NetworkConfigurationException::class
    )
    override fun removeNetwork(vararg ssids: String?) {
        val configuredNetworks = wifiManager.configuredNetworks
        for (conf in configuredNetworks) {
            for (ssid in ssids) {
                if (conf.SSID == ssid || conf.SSID == "\"" + ssid + "\"") { // TODO document why ssid can be surrounded by quotes
                    wifiManager.removeNetwork(conf.networkId)
                    //wifiManager.saveConfiguration(); // not needed, removeNetwork already commits
                    break
                }
            }
        }
        for (ssid in ssids) {
            // We removed all SSIDs, but are they gone?
            // If not, are we at least allowed to override them?
            if (isNetworkConfigured(ssid) && !isNetworkOverrideable(ssid)) {
                throw NetworkConfigurationException("Unable to remove network with SSID \"$ssid\"")
            }
        }
    }

    /**
     * Checks if a network with the given SSID is configured
     *
     * @param ssid Check if a network with this SSID exists
     * @return A network with the given SSID exists
     */
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun isNetworkConfigured(ssid: String?): Boolean {
        val configuredNetworks = wifiManager.configuredNetworks
        for (conf in configuredNetworks) {
            if (conf.SSID == ssid || conf.SSID == "\"" + ssid + "\"") { // TODO document why ssid can be surrounded by quotes
                return true
            }
        }
        return false
    }

    /**
     * Checks if the network with the given SSID can be overridden
     *
     * @param ssid Check if a network with this SSID can be overridden
     * @return The network with the given SSID can be overridden
     */
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun isNetworkOverrideable(ssid: String?): Boolean {
        val configuredNetworks = wifiManager.configuredNetworks
        for (conf in configuredNetworks) {
            if (conf.SSID == ssid || conf.SSID == "\"" + ssid + "\"") { // TODO document why ssid can be surrounded by quotes
                val packageName = context.packageName


                // Return whether the network was made by us
                return conf.toString().lowercase(Locale.getDefault()).contains(
                    packageName.lowercase(
                        Locale.getDefault()
                    )
                ) // TODO document why case insensitive
            }
        }

        // Doesn't exist, so override away
        return true
    }

    /**
     * Removes the passpoint configuration if exists in the device
     *
     * @param id FQDN of the Passpoint configuration
     */
    protected fun removePasspoint(id: String) {
        for (conf in wifiManager.passpointConfigurations) {
            if (id == conf.homeSp.fqdn) {
                try {
                    wifiManager.removePasspointConfiguration(id)
                } catch (e: IllegalArgumentException) {
                    Log.d("Passpoint", "removePasspoint: $e")
                }
            }
        }
    }

    /**
     * Configures the passpoint in the device if this have available passpoint
     *
     * @param config Passpoint configuration
     * @throws SecurityException         When adding the network was disallowed
     * @throws NetworkInterfaceException The network interface does not support Passpoint
     */
    @Throws(SecurityException::class, NetworkInterfaceException::class)
    fun configurePasspoint(config: PasspointConfiguration) {
        try {
            try {
                // Remove any existing networks with the same FQDN
                wifiManager.removePasspointConfiguration(config.homeSp.fqdn)
            } catch (e: IllegalArgumentException) {
                // According to the documentation, IllegalArgumentException can be thrown
                // But after testing, we see that SecurityException will be thrown
                // with message "Permission denied".

                // This error makes sense when observed (maybe we can't remove the network),
                // but it's undocumented that this error can be thrown.
            } catch (e: SecurityException) {
            }
            wifiManager.addOrUpdatePasspointConfiguration(config)
        } catch (e: IllegalArgumentException) {
            // Can throw when configuration is wrong or device does not support Passpoint
            // I'm going to be cocky here, and assume that our code generating the configuration
            // doesn't contain a bug.  During testing, this was the case,
            // while we did encounter a few devices without Passpoint support.
            throw NetworkInterfaceException("Device does not support passpoint", e)
        }
    }
}