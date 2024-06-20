package com.xaye.downloader.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.xaye.downloader.R
import com.xaye.downloader.utils.Constants
import com.xaye.downloader.utils.NotificationConst
import com.xaye.downloader.utils.TextUtil

/**
 * Author xaye
 * @date: 2024/6/17
 */
class NotificationHandler(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)
    private val notificationChannelId = "DOWNLOAD_NOTIFICATION_CHANNEL"
    private var notificationId: Int = 0
    private lateinit var notificationBuilder: NotificationCompat.Builder

    init {
        createNotificationChannel()
        notificationId = generateNotificationId()
        notificationBuilder = initNotificationBuilder("Download")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "Download Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for download status"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun generateNotificationId(): Int {
        return System.currentTimeMillis().toInt()
    }

    private fun initNotificationBuilder(fileName: String): NotificationCompat.Builder {

        // Intent to open the app
        val intentOpen = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("KEY_REQUEST_ID", notificationId)
        }
        val pendingIntentOpen = PendingIntent.getActivity(
            context, notificationId, intentOpen, PendingIntent.FLAG_IMMUTABLE
        )

        // Intent to cancel the download
        val intentCancel = Intent(context, NotificationReceiver::class.java).apply {
            action = "ACTION_NOTIFICATION_CANCELLED"
            putExtra("KEY_NOTIFICATION_ID", notificationId)
        }
        val pendingIntentCancel = PendingIntent.getBroadcast(
            context, notificationId, intentCancel, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Downloading $fileName")
            .setContentIntent(pendingIntentOpen)
            .addAction(0, "Cancel", pendingIntentCancel)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setAutoCancel(false)
    }

    @SuppressLint("MissingPermission")
    fun sendUpdateNotification(fileName: String, progress: Int, speedInBPerMs: Float, length: Long) {
        notificationBuilder
            .setProgress(100, progress, false)
            .setContentText("Speed: ${speedInBPerMs} B/ms, Total: ${length}B")
            .setSubText("$progress%")

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    @SuppressLint("MissingPermission")
    fun sendDownloadFailedNotification(fileName: String) {
        notificationBuilder
            .setContentTitle("Download Failed")
            .setContentText("File $fileName failed to download.")
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setAutoCancel(true)

        notificationManager.notify(notificationId + 1, notificationBuilder.build())
    }

    @SuppressLint("MissingPermission")
    fun sendDownloadCancelledNotification(fileName: String) {
        notificationBuilder
            .setContentTitle("Download Cancelled")
            .setContentText("File $fileName download was cancelled.")
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    @SuppressLint("MissingPermission")
    fun sendDownloadSuccessNotification(fileName: String) {
        notificationBuilder
            .setContentTitle("Download Complete")
            .setContentText("File $fileName has been downloaded successfully.")
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setAutoCancel(true)

        notificationManager.notify(notificationId + 1, notificationBuilder.build())
    }
}