package app.eduroam.geteduroam.config

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings.EXTRA_WIFI_NETWORK_RESULT_LIST
import androidx.activity.result.contract.ActivityResultContract

class WifiConfigResult :
    ActivityResultContract<Intent, WifiConfigResponse>() {
    override fun createIntent(context: Context, input: Intent): Intent = input

    override fun parseResult(resultCode: Int, intent: Intent?): WifiConfigResponse =
        when (resultCode) {
            Activity.RESULT_OK -> {
                val forWifi =
                    intent?.getIntArrayExtra(EXTRA_WIFI_NETWORK_RESULT_LIST) ?: intArrayOf()
                WifiConfigResponse.Success(forWifi)
            }
            Activity.RESULT_CANCELED -> {
                WifiConfigResponse.Canceled
            }
            else -> {
                WifiConfigResponse.Canceled
            }
        }
}

sealed class WifiConfigResponse() {
    data class Success(val forWifi: IntArray) : WifiConfigResponse()
    object Canceled : WifiConfigResponse()
}