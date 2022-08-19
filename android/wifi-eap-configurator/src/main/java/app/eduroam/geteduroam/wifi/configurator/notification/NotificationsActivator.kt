package app.eduroam.geteduroam.wifi.configurator.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Its the class responsable of reactive the alarm when the device is rebooted
 */
class NotificationsActivator : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            StartNotifications.enqueueWorkStart(context, Intent())
        }
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            StartNotifications.enqueueWorkStart(context, Intent().putExtra("expiration", true))
        }
    }
}