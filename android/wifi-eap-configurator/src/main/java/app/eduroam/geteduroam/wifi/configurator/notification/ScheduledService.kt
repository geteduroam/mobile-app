package app.eduroam.geteduroam.wifi.configurator.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import app.eduroam.geteduroam.wifi.configurator.R

/**
 * Its the class responsable of create the notification and send it
 */
@RequiresApi(api = Build.VERSION_CODES.Q)
class ScheduledService : JobIntentService() {
    override fun onHandleWork(intent: Intent) {
        if (intent.getBooleanExtra("expiration", false)) {
            val netId: Int = intent.getIntExtra("netId", -1)
            val fqdn: String? = intent.getStringExtra("fqdn")
            val wm: WifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P || netId < 0) {
                val suggestionList: List<WifiNetworkSuggestion> = ArrayList<WifiNetworkSuggestion>()
                wm.removeNetworkSuggestions(suggestionList)
            } else {
                try {
                    wm.removeNetwork(netId)
                } catch (e: Throwable) { /* do nothing */
                }
                if (fqdn != "") {
                    try {
                        wm.removePasspointConfiguration(fqdn)
                    } catch (e: Throwable) { /* do nothing */
                    }
                }
            }
        } else {
            // First we create the channel of the notifications
            val channel1 =
                NotificationChannel("channel1", "geteduroam", NotificationManager.IMPORTANCE_HIGH)
            channel1.description = "geteduroam App"
            val manager: NotificationManager = applicationContext.getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(channel1)

            // Create an Intent for the activity you want to start
            val resultIntent = Intent(applicationContext, NotificationActivity::class.java)
            // Create the TaskStackBuilder and add the intent, which inflates the back stack
            val stackBuilder = TaskStackBuilder.create(applicationContext)
            stackBuilder.addNextIntentWithParentStack(resultIntent)
            // Get the PendingIntent containing the entire back stack
            val resultPendingIntent: PendingIntent? =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

            // Create the notification
            val mBuilder = NotificationCompat.Builder(applicationContext, "channel1")
            mBuilder.setSmallIcon(R.drawable.ic_notifications)
                .setColor(0x005da9)
                .setContentTitle(intent.getStringExtra("title"))
                .setContentText(intent.getStringExtra("message"))
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
            manager.notify(123, mBuilder.build())
        }
    }

    companion object {
        const val JOB_ID = 2
        fun enqueueWorkSchedule(context: Context?, work: Intent) {
            enqueueWork(context!!, ScheduledService::class.java, JOB_ID, work)
        }
    }
}