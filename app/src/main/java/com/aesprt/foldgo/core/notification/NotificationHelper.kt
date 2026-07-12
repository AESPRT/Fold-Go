package com.aesprt.foldgo.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.aesprt.foldgo.MainActivity
import com.aesprt.foldgo.R
import java.util.Locale

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

    /**
     * Show notification when a batch completes on a machine
     * Example: "Batch 1 (8kg) has finished wash cycle on Speed Queen"
     */
    fun showBatchCompletionNotification(
        machineName: String,
        batchWeight: Double,
        batchStatus: String,
        orderNumber: String,
        batchId: String,
        orderId: String
    ) {
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("orderId", orderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            batchId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Format: "Batch #1 (8.0kg) has finished wash cycle on Speed Queen"
        val contentText = "Order Number $orderNumber (${String.format(Locale.getDefault(), "%.1f", batchWeight)}kg) has finished $batchStatus on $machineName"

        val notification = NotificationCompat.Builder(context, MACHINE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome) // Replace with app icon later
            .setContentTitle("Cycle Complete")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 500, 1000, 500))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(batchId.hashCode(), notification)
    }

    /**
     * Fallback notification when batch info is not available
     */
    fun showMachineCompletionNotification(machineName: String, orderId: String? = null) {
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            orderId?.let { putExtra("orderId", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            machineName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, MACHINE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome) // Replace with app icon later
            .setContentTitle("Cycle Complete")
            .setContentText("Machine $machineName has finished its cycle.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 500, 1000, 500))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(machineName.hashCode(), notification)
    }
}