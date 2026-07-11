package com.aesprt.foldgo.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {

    companion object {
        const val MACHINE_CHANNEL_ID = "machine_alerts"
        const val MACHINE_CHANNEL_NAME = "Machine Alerts"
    }

    fun createNotificationChannels() {
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val channel = NotificationChannel(
            MACHINE_CHANNEL_ID,
            MACHINE_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for machine cycle completion"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 1000, 500)
            setSound(alarmSound, audioAttributes)
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun showMachineCompletionNotification(machineName: String) {
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val notification = NotificationCompat.Builder(context, MACHINE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with app icon later
            .setContentTitle("Cycle Complete")
            .setContentText("Machine $machineName has finished its cycle.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 500, 1000, 500))
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(machineName.hashCode(), notification)
    }
}
