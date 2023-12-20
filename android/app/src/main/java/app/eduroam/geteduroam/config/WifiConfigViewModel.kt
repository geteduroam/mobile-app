package app.eduroam.geteduroam.config

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.net.wifi.hotspot2.PasspointConfiguration
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
import app.eduroam.geteduroam.ui.theme.IS_EDUROAM
import app.eduroam.geteduroam.ui.theme.isChromeOs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WifiConfigViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
    ) : ViewModel() {

    lateinit var eapIdentityProviderList: EAPIdentityProviderList

    val launch: MutableStateFlow<Unit?> = MutableStateFlow(null)
    val progressMessage: MutableStateFlow<String> = MutableStateFlow("")
    val requestChangeNetworkPermission: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val processing: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val intentWithSuggestions: MutableStateFlow<Intent?> = MutableStateFlow(null)
    val showUsernameDialog: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val didEnterUserCredentials: MutableStateFlow<Boolean> = MutableStateFlow(false)

    init {
        launch.value = Unit
    }

    fun launchConfiguration(context: Context) = viewModelScope.launch {
        launch.value = null
        if (eapIdentityProviderList.eapIdentityProvider?.firstOrNull()?.requiresUsernamePrompt() == true && !didEnterUserCredentials.value) {
            showUsernameDialog.value = true
            return@launch
        }

        when {
            //Android 11 and higher - API 30 - ChromeOS - we show everything in one intent
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && context.isChromeOs() -> {
                handleAndroid11ChromeOs()
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && IS_EDUROAM -> {
                // We will use Intent for SSID, and Suggestion for Passpoint.
                // It would've been possible to use Intent for both SSID and Passpoint,
                // but we can never remove intents for Passpoint.
                // Android will overwrite existing intents if it considers the new Passpoint configuration similar enough,
                // but it considers the certificate (among other things) in checking for equality, so "similar" is too high a bar.
                // For SSIDs it works well, it's considered equal if the SSID matches.
                // This is why we use intents for SSIDs and suggestions for Passpoint.

                // We don't use suggestions for SSIDs (if we can avoid it) because it would confuse users;
                // suggestions are prioritized under user-configured (e.g. has ever connected to) networks,
                // which includes any onboarding guest network, nearby Starbucks and lab-raspberry-pi setup.
                // Worse, when the user does notice this and explicitly wants to connect to eduroam,
                // the network appears unconfigured in the Wi-Fi picker.
                // This would confuse both users and help desks.
                handleAndroid11PhoneOrTablet(context)
            }
            //Android 10 - API 29
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // Not using intent, only suggestions, since the API is not available
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

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun removeNetworks(context: Context) {
        val wifiManager: WifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        // Empty list removes all networks
        wifiManager.removeNetworkSuggestions(emptyList<WifiNetworkSuggestion>())
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun removeNetworks(context: Context, vararg ssids: String) {
        val wifiManager: WifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (ssids.isNotEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val suggestions: MutableList<WifiNetworkSuggestion> = wifiManager.networkSuggestions
            for (suggestion in suggestions) {
                for (ssid in ssids) {
                    if (ssid == suggestion.ssid) break
                }
                suggestions.remove(suggestion)
            }
            wifiManager.removeNetworkSuggestions(suggestions)
        } else {
            removeNetworks(context)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun handleAndroid11ChromeOs() {
        // We don't remove networks here, because networks added by an intent cannot be removed.
        val suggestions = eapIdentityProviderList.buildAllNetworkSuggestions()
        val intent = createSuggestionsIntent(suggestions = suggestions)
        intentWithSuggestions.value = intent
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun handleAndroid11PhoneOrTablet(context: Context) {
        val suggestions = eapIdentityProviderList.buildSSIDSuggestions()
        val intent = createSuggestionsIntent(suggestions = suggestions)
        intentWithSuggestions.value = intent
        val passPointSuggestion = eapIdentityProviderList.buildPasspointSuggestion()
        if (passPointSuggestion != null) {
            removeNetworks(context)
            val wifiManager: WifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            try {
                val status = wifiManager.addNetworkSuggestions(listOf(passPointSuggestion))
                if (status != 0) {
                    Timber.e("Status for adding network: $status")
                } else {
                    Timber.i("Successfully added network.")
                }
            } catch (e: Exception) {
                progressMessage.value = "Failed to add Passpoint suggestion. Exception: ${e.message}"
                Timber.e(e, "Failed to add network suggestion")
            }
        }
        processing.value = false
    }

    private fun PasspointConfiguration.install(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            throw RuntimeException("This method should not be called on this device! Use buildPasspointSuggestion()!")
        } else {
            try {
                val wifiManager: WifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                try {
                    // Remove any existing networks with the same FQDN
                    @Suppress("DEPRECATION")
                    wifiManager.removePasspointConfiguration(this.homeSp.fqdn)
                } catch (e: java.lang.IllegalArgumentException) {
                    // According to the documentation, IllegalArgumentException can be thrown
                    // But after testing, we see that SecurityException will be thrown
                    // with message "Permission denied".

                    // This error makes sense when observed (maybe we can't remove the network),
                    // but it's undocumented that this error can be thrown.
                } catch (e: SecurityException) {
                    // Ignore
                }
                wifiManager.addOrUpdatePasspointConfiguration(this)
            } catch (e: IllegalArgumentException) {
                // Can throw when configuration is wrong or device does not support Passpoint
                // while we did encounter a few devices without Passpoint support.
                progressMessage.value = "Failed to add Passpoint. Exception: ${e.message}"
                Timber.e(e, "Failed to add or update Passpoint config")
            }
        }
    }

    /**
     * Requires CHANGE_WIFI_STATE permission
     * */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun handleAndroid10WifiConfig(context: Context) {
        val ssidSuggestions = eapIdentityProviderList.buildSSIDSuggestions()
        val wifiManager: WifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ssids = eapIdentityProviderList.eapIdentityProvider?.firstOrNull()?.credentialApplicability?.mapNotNull { it.ssid } ?: emptyList()
        removeNetworks(context, *ssids.toTypedArray())

        try {
            val status = wifiManager.addNetworkSuggestions(ssidSuggestions)
            if (status != 0) {
                Timber.e("Status for adding network: $status")
            } else {
                Timber.i("Successfully added network.")
            }
        } catch (e: Exception) {
            progressMessage.value = "Failed to add WiFi Suggestions. Exception: ${e.message}"
            Timber.e(e, "Failed to add network suggestion")
        }

        val passpointConfig = eapIdentityProviderList.buildPasspointConfig()
        try {
            passpointConfig?.install(context)
        } catch (e: IllegalArgumentException) {
            // Can throw when configuration is wrong or device does not support Passpoint
            // while we did encounter a few devices without Passpoint support.
            progressMessage.value = "Failed to add Passpoint. Exception: ${e.message}"
            Timber.e(e, "Failed to add or update Passpoint config")

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
        passpointConfig?.install(context)
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

    fun didEnterLoginDetails(username: String, password: String) {
        eapIdentityProviderList.eapIdentityProvider?.forEach { idp ->
            idp.authenticationMethod?.forEach { authMethod ->
                authMethod.clientSideCredential?.apply {
                    this.userName = username
                    this.password = password
                }
            }
        }
        showUsernameDialog.value = false
        didEnterUserCredentials.value = true
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