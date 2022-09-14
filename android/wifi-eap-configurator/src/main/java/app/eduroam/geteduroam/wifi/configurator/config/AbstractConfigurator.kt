package app.eduroam.geteduroam.wifi.configurator.config

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.preference.PreferenceManager
import androidx.annotation.RequiresPermission
import app.eduroam.geteduroam.wifi.configurator.exception.NetworkConfigurationException
import app.eduroam.geteduroam.wifi.configurator.exception.NetworkInterfaceException
import app.eduroam.geteduroam.wifi.configurator.notification.StartNotifications
import app.eduroam.geteduroam.wifi.configurator.notification.StartRemoveNetwork

/**
 * The configurator is used to install a `WifiProfile` in the device
 *
 *
 * Due to Android changing their Wi-Fi API considerably from Android 9 through 11 (API 28-30),
 * this class is abstract and has API-specific subclasses.  The old API calls (from API 28) are
 * arguably the most user-friendly, in that they do what the user would most expect.  After running
 * the app, the Wi-Fi network is configured, after running the app again, the network is updated,
 * and after deleting the app, the Wi-Fi network is deleted.  This is done with the
 * `WifiManager.addNetwork` API.
 *
 *
 * Sadly, this API is only limited available from API 29 (available for Passpoint, not for SSID),
 * and completely unavailable from API 30 and onwards.  The replacement is the Suggestions API,
 * which provide the user with a dialog asking them if they want to accept the network from our app.
 * On Android 10, this does not always work the way one would expect; notifications are often not
 * visible to the user and it gives the impression things are just not working.  On Android 11
 * it works better, with the notifications showing up at the time they're being configured.
 */
abstract class AbstractConfigurator internal constructor(protected val context: Context) {
    @JvmField
    protected val wifiManager: WifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    /**
     * Remove networks with matching SSIDs
     *
     * @param ssids Remove network matching these SSIDs
     * @throws NetworkConfigurationException A network was not removed (does not throw if the network was not configured to begin with)
     */
    @Throws(NetworkConfigurationException::class)
    abstract fun removeNetwork(vararg ssids: String?)

    /**
     * Enable wifi of the device
     *
     * @throws NetworkInterfaceException Wi-Fi could not be enabled
     */
    @Throws(NetworkInterfaceException::class)
    fun enableWifi() {
        if (!wifiManager.setWifiEnabled(true)) {
            throw NetworkInterfaceException("Unable to enable Wi-Fi on the device")
            //throw new WifiEapConfiguratorException("plugin.wifieapconfigurator.error.wifi.disabled");
        }
    }

    /**
     * Check if the SSID is in range
     *
     * @param ssid SSID to check
     * @return SSID is in range
     */
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun reachableSSID(ssid: String?): Boolean {
        require(!(ssid == null || "" == ssid)) {
            "No SSID provided"
            //throw new WifiEapConfiguratorException("plugin.wifieapconfigurator.error.ssid.missing");
        }

        // TODO is this check really necessary? Can't we just check the actual permission?
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        check(location) {
            "Location services is disabled"
            //throw new WifiEapConfiguratorException("plugin.wifieapconfigurator.error.location.disabled");
        }
        for (s in wifiManager.scanResults) {
            if (s.SSID == ssid || s.SSID == "\"" + ssid + "\"") { // TODO document why ssid can be surrounded by quotes
                return true
            }
        }
        return false
    }

    /**
     * Check if the device is currently connected to the given SSID
     *
     * @param ssid SSID to check
     * @return
     */
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun isConnectedSSID(ssid: String?): Boolean {
        require(!(ssid == null || "" == ssid)) { "No SSID provided" }

        // TODO is this check really necessary? Can't we just check the actual permission?
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        check(location) { "Location services is disabled" }
        val info = wifiManager.connectionInfo
        val connectedSSID = info.ssid

        // WifiInfo#getSSID() documentation states that the SSID is surrounded by quotes,
        // iff it can be decoded as UTF8
        return "\"" + ssid + "\"" == connectedSSID
    }

    /**
     * Determines if any of the specified SSIDs are configured on this device
     *
     * @param ssids SSIDs to check
     * @return At least one of the SSIDs is configured
     */
    fun areAnyNetworksConfigured(vararg ssids: String?): Boolean {
        for (ssid in ssids) {
            if (isNetworkConfigured(ssid)) return true
        }
        return false
    }

    /**
     * Checks if a network with the given SSID is configured
     *
     * @param ssid Check if a network with this SSID exists
     * @return A network with the given SSID exists
     */
    abstract fun isNetworkConfigured(ssid: String?): Boolean

    /**
     * Checks if the network with the given SSID can be overridden
     *
     * @param ssid Check if a network with this SSID can be overridden
     * @return The network with the given SSID can be overridden
     */
    abstract fun isNetworkOverrideable(ssid: String?): Boolean

    /**
     * Check if the Wi-Fi is enabled on the device
     *
     * @return Wi-Fi is enabled
     */
    fun checkEnabledWifi(): Boolean {
        return wifiManager.isWifiEnabled
    }

    /**
     * Write the institution ID to the application store
     *
     * @param institutionID Institution ID to store
     */
    fun writeToSharedPref(institutionID: String?) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPref.edit()
        editor.putString("institutionId", institutionID)
        editor.apply()
    }

    /**
     * Send a notification at the specified date
     *
     *
     * TODO what's the date format?
     *
     * @param date
     * @param title
     * @param message
     */
    fun sendNotification(date: String?, title: String?, message: String?) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPref.edit()
        editor.putString("date", date)
        editor.putString("title", title)
        editor.putString("message", message)
        editor.apply()
        StartNotifications.enqueueWorkStart(context, Intent())
        setExpireNetwork(context)
    }

    /**
     * Reads the institutionId saved in the SharedPref of the app
     *
     * @return Stored institution ID
     */
    fun readFromSharedPref(): String? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPref.getString("institutionId", "")
    }

    companion object {
        fun setExpireNetwork(context: Context?) {
            val intent = Intent()
            intent.putExtra("expiration", true)
            StartRemoveNetwork.enqueueWorkStart(context, intent)
        }
    }
}