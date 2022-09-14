package app.eduroam.geteduroam.wifi.configurator.exception

import android.net.wifi.WifiManager
import android.util.Log

/**
 * An exception indication that installing `WifiNetworkSuggestion`s failed
 */
class NetworkSuggestionException : NetworkException {
    /**
     * Construct a new exception
     *
     * @param status The status as returned by `WifiManager#addNetworkSuggestions`
     * @see WifiManager.addNetworkSuggestions
     */
    constructor(status: Int) : super(getMessageFromStatus(status)) {}

    /**
     * Construct a new exception
     *
     * @param status The status as returned by `WifiManager#addNetworkSuggestions`
     * @param cause  The exception that caused this error
     * @see WifiManager.addNetworkSuggestions
     */
    internal constructor(status: Int, cause: Throwable?) : super(
        getMessageFromStatus(status),
        cause
    ) {
    }

    companion object {
        private fun getMessageFromStatus(status: Int): String {
            return when (status) {
                WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS -> "STATUS_NETWORK_SUGGESTIONS_SUCCESS"
                WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_INTERNAL -> "STATUS_NETWORK_SUGGESTIONS_ERROR_INTERNAL"
                WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_APP_DISALLOWED -> "STATUS_NETWORK_SUGGESTIONS_ERROR_APP_DISALLOWED"
                WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_DUPLICATE -> {
                    // On Android 11, this can't happen according to the documentation
                    // On Android 10, this should not happen because we removed all networks earlier
                    Log.e(
                        "NetworkSuggestionException",
                        "STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_DUPLICATE occurred, this should not happen!"
                    )
                    "STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_DUPLICATE"
                }
                WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_EXCEEDS_MAX_PER_APP -> "STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_EXCEEDS_MAX_PER_APP"
                WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_REMOVE_INVALID -> "STATUS_NETWORK_SUGGESTIONS_ERROR_REMOVE_INVALID"
                WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_NOT_ALLOWED -> "STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_NOT_ALLOWED"
                WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_INVALID -> "STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_INVALID"
                else -> "UNKNOWN_ERROR"
            }
        }
    }
}