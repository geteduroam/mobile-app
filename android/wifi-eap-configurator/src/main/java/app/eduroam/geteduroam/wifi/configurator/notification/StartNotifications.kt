package app.eduroam.geteduroam.wifi.configurator.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.SystemClock
import android.preference.PreferenceManager
import androidx.annotation.RequiresApi
import androidx.core.app.JobIntentService
import java.util.*

/**
 * Its the class responsable of create the alarm to send the notification
 */
class StartNotifications : JobIntentService() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onHandleWork(intent: Intent) {
        val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(
            applicationContext
        )
        if ("" != sharedPref.getString("date", "")) {
            val stringDate: String? = sharedPref.getString("date", "")
            val title: String? = sharedPref.getString("title", "")
            val message: String? = sharedPref.getString("message", "")
            val dateUntil = stringDate?.toLong() ?: 0
            val dateNow = Date()
            val millisNow = dateNow.time
            val delay = dateUntil - millisNow - 432000000
            if (delay > 0L) {
                val mgr: AlarmManager =
                    applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
                val i = Intent(applicationContext, NotificationReceiver::class.java)
                i.putExtra("title", title)
                i.putExtra("message", message)
                val pi: PendingIntent = PendingIntent.getBroadcast(applicationContext, 0, i, 0)
                mgr.set(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + delay,
                    pi
                )
            }
        }
    }

    companion object {
        const val JOB_ID = 1

        @JvmStatic
        fun enqueueWorkStart(context: Context?, work: Intent) {
            enqueueWork(context!!, StartNotifications::class.java, JOB_ID, work)
        }
    }
}