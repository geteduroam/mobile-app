package app.eduroam.geteduroam

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.net.wifi.hotspot2.PasspointConfiguration
import android.net.wifi.hotspot2.pps.HomeSp
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import app.eduroam.geteduroam.oauth.OAuthViewModel
import app.eduroam.geteduroam.ui.theme.AppTheme
import app.eduroam.shared.BuildConfig
import app.eduroam.shared.injectLogger
import app.eduroam.shared.profile.SelectProfileViewModel
import app.eduroam.shared.select.SelectInstitutionViewModel
import co.touchlab.kermit.Logger
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent

class MainActivity : ComponentActivity(), KoinComponent {
    private val log: Logger by injectLogger("MainActivity")
    private val institutionViewModel: SelectInstitutionViewModel by viewModel()
    private val profileViewModel: SelectProfileViewModel by viewModel()
    private val oAuthViewModel: OAuthViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        intent.data?.let {
            val code = it.getQueryParameter("code")
            val institutionId = it.getQueryParameter("state")
            // get Institution by ID
            // get token url Oauth2.getTokenUrl(Institution, code)
            // select/download config
        }
        setContent {
            AppTheme {
                NavGraph(
                    viewModel = institutionViewModel,
                    profileViewModel = profileViewModel,
                    oauthViewModel = oAuthViewModel,
                    log = log
                )
            }
        }
    }

    fun configureWifi() {
        val suggestion1 = WifiNetworkSuggestion.Builder().setSsid("test111111")
            .setIsAppInteractionRequired(true) // Optional (Needs location permission)
            .build();

        val suggestion2 =
            WifiNetworkSuggestion.Builder().setSsid("test222222").setWpa2Passphrase("test123456")
                .setIsAppInteractionRequired(true) // Optional (Needs location permission)
                .build();

        val suggestion3 =
            WifiNetworkSuggestion.Builder().setSsid("test333333").setWpa3Passphrase("test6789")
                .setIsAppInteractionRequired(true) // Optional (Needs location permission)
                .build();

        val passpointConfig = PasspointConfiguration()
        val homeSp = HomeSp()
        homeSp.fqdn = "dummy FQDN"
        homeSp.friendlyName = "dummy FQDN via Passpoint"
        homeSp.roamingConsortiumOis = listOf("001bc50460").map { it.toLong(16) }.toLongArray()
        passpointConfig.homeSp = homeSp
//        val suggestion4 = WifiNetworkSuggestion.Builder().setPasspointConfig(passpointConfig)
//            .setIsAppInteractionRequired(true) // Optional (Needs location permission)
//            .build();

        val suggestionsList = listOf(suggestion1, suggestion2, suggestion3);
        Log.e("CheckConfig", "Running 1")
        val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager;
        try {
            wifiManager.removeNetworkSuggestions(suggestionsList)
            Log.e("CheckConfig", "Successfully removed known networks")
        } catch (e: Exception) {
            Log.e("CheckConfig", "Failed to remove existing networks")
        }
        Log.e("CheckConfig", "Running 2")
        try {
            wifiManager.addOrUpdatePasspointConfiguration(passpointConfig)
            Log.e("CheckConfig", "Added/updated passpoint")
        } catch (e: Exception) {
            Log.e("CheckConfig", "Failed to add or update passpoint configuration", e)
        }

        val status = wifiManager.addNetworkSuggestions(suggestionsList);
        if (status != WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
            Log.e("CheckConfig", "failed to config dummy wifi. Status code: $status: ")
        }

// Optional (Wait for post connection broadcast to one of your suggestions)
        val intentFilter = IntentFilter(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION);

        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (!intent.action.equals(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION)) {
                    return;
                }
                Log.d(
                    "CheckConfig", "onReceive() called with: intent = $intent"
                )
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);

    }
}