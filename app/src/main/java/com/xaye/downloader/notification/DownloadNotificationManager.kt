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
import com.xaye.downloader.utils.NotificationConst
import com.xaye.downloader.utils.TextUtil

/**
 * Author xaye
 * @date: 2024/6/17
 */
class DownloadNotificationManager(
    private val context: Context,
    private val notificationChannelName: String,
    private val notificationChannelDescription: String,
    private val notificationImportance: Int,
    private val requestId: Int,
    private val notificationSmallIcon: Int,
    private val fileName: String
) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val notificationChannelId = NotificationConst.NOTIFICATION_CHANNEL_ID
    private val notificationId = ((requestId + System.currentTimeMillis()) % Int.MAX_VALUE).toInt()
    private val notificationBuilder: NotificationCompat.Builder

    private val pendingIntentOpen: PendingIntent by lazy {
        PendingIntent.getActivity(
            context, notificationId,
            context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(NotificationConst.KEY_NOTIFICATION_ID, notificationId)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    init {
        createNotificationChannel()
        notificationBuilder = initNotificationBuilder()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                notificationChannelName,
                notificationImportance
            ).apply {
                description = notificationChannelDescription
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initNotificationBuilder(): NotificationCompat.Builder {

        val pendingIntentCancel = PendingIntent.getBroadcast(
            context, notificationId,
            Intent(context, NotificationReceiver::class.java).apply {
                action = NotificationConst.ACTION_NOTIFICATION_DISMISSED
                putExtra(NotificationConst.KEY_NOTIFICATION_ID, notificationId)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(notificationSmallIcon)
            .setContentTitle("$fileName 下载中")
            .setContentIntent(pendingIntentOpen)
            .addAction(0, "", pendingIntentCancel)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setAutoCancel(false)
    }

    @SuppressLint("MissingPermission")
    fun sendUpdateNotification(progress: Int, speedInBPerMs: Float, length: Long) {
        notificationBuilder
            .setProgress(100, progress, false)
            .setContentText("下载速度: ${TextUtil.getSpeedText(speedInBPerMs)}, 大小: ${TextUtil.getTotalLengthText(length)}")
            .setSubText("$progress%")
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    @SuppressLint("MissingPermission")
    fun sendDownloadFailedNotification() {
        notificationBuilder.setProgress(0, 0, false)
        notificationManager.notify(notificationId, notificationBuilder.build())
        updateNotification("$fileName 下载失败", "文件 $fileName 下载失败！")
    }

    @SuppressLint("MissingPermission")
    fun sendDownloadCancelledNotification() {
        notificationBuilder.setProgress(0, 0, false)
        notificationManager.notify(notificationId, notificationBuilder.build())
        updateNotification(fileName, "下载取消！")
    }

    @SuppressLint("MissingPermission")
    fun sendDownloadPausedNotification() {
        notificationBuilder.setProgress(0, 0, false)
        notificationManager.notify(notificationId, notificationBuilder.build())
        updateNotification(fileName, "下载暂停！")
    }

    @SuppressLint("MissingPermission")
    fun sendDownloadSuccessNotification(size: String) {
        notificationBuilder.setProgress(0, 0, false)
        notificationManager.notify(notificationId, notificationBuilder.build())
        updateNotification(fileName, "文件下载成功($size)")
    }

    private fun updateNotification(title: String, contentText: String) {

        val updatedNotificationBuilder = NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(notificationSmallIcon)
            .setContentTitle(title)
            .setContentText(contentText)
            .setContentIntent(pendingIntentOpen)
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, updatedNotificationBuilder.build())
    }
}

