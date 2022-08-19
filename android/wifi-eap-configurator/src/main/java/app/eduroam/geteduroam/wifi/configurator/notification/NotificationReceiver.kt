package app.eduroam.geteduroam.wifi.configurator.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Its the class responsable of the init the service of the notifications
 */
@RequiresApi(api = Build.VERSION_CODES.Q)
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.getBooleanExtra("expiration", false)) {
            ScheduledService.enqueueWorkSchedule(
                context,
                Intent().putExtra("expiration", true)
                    .putExtra("netId", intent.getIntExtra("netId", -1))
                    .putExtra("fqdn", intent.getStringExtra("fqdn"))
            )
        } else {
            ScheduledService.enqueueWorkSchedule(
                context,
                Intent().putExtra("title", intent.getStringExtra("title"))
                    .putExtra("message", intent.getStringExtra("message"))
            )
        }
    }
}