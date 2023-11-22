package app.eduroam.geteduroam.config

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.eduroam.geteduroam.config.model.EAPIdentityProviderList
import app.eduroam.geteduroam.di.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class WifiConfigViewModel(
    private val notificationRepository: NotificationRepository
    ) :
    ViewModel() {

    lateinit var eapIdentityProviderList: EAPIdentityProviderList

    val launch: MutableStateFlow<Unit?> = MutableStateFlow(null)
    val progressMessage: MutableStateFlow<String> = MutableStateFlow("")
    val requestChangeNetworkPermission: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val processing: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val intentWithSuggestions: MutableStateFlow<Intent?> = MutableStateFlow(null)

    init {
        launch.value = Unit
    }

    fun launchConfiguration(context: Context) = viewModelScope.launch {
        launch.value = null

        when {
            //Android 11 and higher - API 30
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                handleAndroid11AndOver()
            }
            //Android 10 - API 29
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                if (hasPermission(context)) {
                    handleAndroid10WifiConfig(context)
                } else {
                    requestChangeNetworkPermission.value = true
                }
            }
            //All things Android 9 and lower - API 28
            else -> {
                handleAndroid9AndLower(context)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun handleAndroid11AndOver() {
        val suggestions = eapIdentityProviderList.buildAllNetworkSuggestions()
        val intent = createSuggestionsIntent(suggestions = suggestions)
        intentWithSuggestions.value = intent
    }

    /**
     * Requires CHANGE_WIFI_STATE permission
     * */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun handleAndroid10WifiConfig(context: Context) {
        val ssidSuggestions = eapIdentityProviderList.buildSSIDSuggestions()
        val wifiManager: WifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        try {
            wifiManager.removeNetworkSuggestions(ssidSuggestions)
        } catch (e: Exception) {
            progressMessage.value = "Failed to remove WiFi Suggestions. Exception: ${e.message}"
            Timber.e(e, "Failed to clear previously added network suggestions")
        }

        try {
            val status = wifiManager.addNetworkSuggestions(ssidSuggestions)
            Timber.e( "Status for adding network: $status")
        } catch (e: Exception) {
            progressMessage.value = "Failed to add WiFi Suggestions. Exception: ${e.message}"
            Timber.e(e, "Failed to add network suggestion")
        }

        val passpointConfig = eapIdentityProviderList.buildPasspointConfig()
        if (passpointConfig != null) {
            try {
                wifiManager.addOrUpdatePasspointConfiguration(passpointConfig)
            } catch (e: IllegalArgumentException) {
                // Can throw when configuration is wrong or device does not support Passpoint
                // while we did encounter a few devices without Passpoint support.
                progressMessage.value = "Failed to add Passpoint. Exception: ${e.message}"
                Timber.e(e, "Failed to add or update Passpoint config")

            }
        }
        processing.value = false
    }

    @Suppress("DEPRECATION")
    private fun handleAndroid9AndLower(context: Context) {
        val wifiConfigs = eapIdentityProviderList.buildWifiConfigurations()
        val passpointConfig = eapIdentityProviderList.buildPasspointConfig()

        val wifiManager: WifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        wifiConfigs.forEach { wifiConfig ->
            try {
                val networkId = wifiManager.addNetwork(wifiConfig)
                val didAddNetwork = networkId != -1
                if (didAddNetwork) {
                    wifiManager.disconnect()
                    wifiManager.enableNetwork(networkId, true)
                    wifiManager.reconnect()
                    wifiManager.isWifiEnabled = true
                }
            } catch (e: Exception) {
                progressMessage.value =
                    "Failed to add/connect WifiConfiguration. Exception: ${e.message}"
                Log.e("WifiConfigViewModel", "Failed to add/connect WifiConfiguration", e)
            }
        }

        if (passpointConfig != null) {
            try {
                wifiManager.addOrUpdatePasspointConfiguration(passpointConfig)
            } catch (e: IllegalArgumentException) {
                // Can throw when configuration is wrong or device does not support Passpoint
                // while we did encounter a few devices without Passpoint support.
                progressMessage.value = "Failed to add Passpoint. Exception: ${e.message}"
                Log.e("WifiConfigViewModel", "Failed to Passpoint", e)

            }
        }
        processing.value = false
    }

    private fun hasPermission(context: Context): Boolean = ContextCompat.checkSelfPermission(
        context, Manifest.permission.CHANGE_WIFI_STATE
    ) == PackageManager.PERMISSION_GRANTED

    @RequiresApi(Build.VERSION_CODES.R)
    private fun createSuggestionsIntent(suggestions: List<WifiNetworkSuggestion>?): Intent {
        val forBundle = ArrayList<WifiNetworkSuggestion>()
        if (suggestions != null) {
            forBundle.addAll(suggestions)
        }
        val bundle = Bundle().apply {
            putParcelableArrayList(Settings.EXTRA_WIFI_NETWORK_LIST, forBundle)
        }
        val intent = Intent(Settings.ACTION_WIFI_ADD_NETWORKS)
        return intent.apply {
            putExtras(bundle)
        }
    }

    fun consumeSuggestionIntent() {
        intentWithSuggestions.value = null
    }

    fun markAsComplete() {
        processing.value = false
    }

    fun shouldRequestPushPermission() : Boolean {
        eapIdentityProviderList.eapIdentityProvider?.firstOrNull()?.let {
            return notificationRepository.shouldRequestPushPermission(it)
        }
        return false
    }

    fun scheduleReminderNotification() {
        eapIdentityProviderList.eapIdentityProvider?.firstOrNull()?.let {
            return notificationRepository.scheduleNotificationIfNeeded(it)
        }
    }

}