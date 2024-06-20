package com.xaye.downloader.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.xaye.downloader.utils.NotificationConst

/**
 * Author xaye
 * @date: 2024/6/17
 */
internal class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(NotificationConst.KEY_NOTIFICATION_ID, 0)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }
}