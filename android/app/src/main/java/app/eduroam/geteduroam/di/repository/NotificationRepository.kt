package app.eduroam.geteduroam.di.repository

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.getActivity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import app.eduroam.geteduroam.MainActivity
import app.eduroam.geteduroam.R
import app.eduroam.geteduroam.Route
import app.eduroam.geteduroam.config.model.EAPIdentityProvider
import app.eduroam.geteduroam.config.requiresUsernamePrompt
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.Date


class NotificationRepository(
    @ApplicationContext private val context: Context
){

    companion object {
        const val NOTIFICATION_KEY_PROVIDER_ID = "provider_id"
        const val NOTIFICATION_CHANNEL_ID = "reconfiguration_reminders"
        const val REMIND_DAYS_BEFORE_EXPIRY = 5
        const val NOTIFICATION_ID = 100
    }

    fun shouldRequestPushPermission(provider: EAPIdentityProvider): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // We already have the permission
            return false
        }
        // Check if we actually need it
        return getReminderDate(provider) != null
    }

    private fun getReminderDate(provider: EAPIdentityProvider): Date? {
        if (provider.requiresUsernamePrompt()) {
            // Only OAuth users require notification reminders
            return null
        }
        val validUntil = provider.validUntil ?: return null
        val now = Date()
        val expiryReminderAtMs = validUntil.time - REMIND_DAYS_BEFORE_EXPIRY * 24 * 60 * 60 * 1000
        if (expiryReminderAtMs > now.time) {
            return Date(expiryReminderAtMs)
        }
        return null
    }
    fun scheduleNotificationIfNeeded(provider: EAPIdentityProvider) {
        val reminderDate = getReminderDate(provider) ?: return
        createNotificationChannel()
        Timber.i("Posting reminder to date: $reminderDate")
        postNotificationAtDate(reminderDate, provider)
    }

    private fun postNotificationAtDate(reminderDate: Date, provider: EAPIdentityProvider) {
        val intent = Intent(context, NotificationAlarmReceiver::class.java)
        intent.putExtra(NOTIFICATION_KEY_PROVIDER_ID, provider.ID)
        val pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = context.applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, reminderDate.time, pendingIntent)
    }

    private fun createNotificationChannel() {
        val name = context.getString(R.string.notification_channel_name)
        val description = context.getString(R.string.notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)
        channel.description = description
        val notificationManager: NotificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}

class NotificationAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) {
            return
        }
        val tapResultIntent = Intent(context, MainActivity::class.java)
        intent?.getStringExtra(NotificationRepository.NOTIFICATION_KEY_PROVIDER_ID)?.let {
            tapResultIntent.data = Uri.parse(Route.SelectProfile.buildDeepLink(it))
        }
        tapResultIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent: PendingIntent = getActivity( context, 0, tapResultIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, NotificationRepository.NOTIFICATION_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notification_title, context.getString(R.string.name)))
                .setContentText(context.getString(R.string.notification_message, NotificationRepository.REMIND_DAYS_BEFORE_EXPIRY, context.getString(R.string.name)))
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build()
        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(NotificationRepository.NOTIFICATION_ID, notification)
        } else {
            Timber.w("Could not post notification because there was no permission!")
        }
    }
}