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

class StartRemoveNetwork : JobIntentService() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onHandleWork(intent: Intent) {
        val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(
            applicationContext
        )
        val date: String? = sharedPref.getString("date", "")
        val netId: Int = sharedPref.getInt("netId", -1)
        val fqdn: String? = sharedPref.getString("fqdn", "")
        val dateUntil = date?.toLong() ?: 0L
        val dateNow = Date()
        val millisNow = dateNow.time
        val delay = dateUntil - millisNow
        if (delay > 0L) {
            val mgr: AlarmManager =
                applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
            val i = Intent(applicationContext, NotificationReceiver::class.java)
            i.putExtra("expiration", true)
            i.putExtra("netId", netId)
            i.putExtra("fqdn", fqdn)
            val pi: PendingIntent = PendingIntent.getBroadcast(applicationContext, 1, i, 0)
            mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delay, pi)
        }
    }

    companion object {
        const val JOB_ID = 3

        @JvmStatic
        fun enqueueWorkStart(context: Context?, work: Intent) {
            enqueueWork(context!!, StartRemoveNetwork::class.java, JOB_ID, work)
        }
    }
}