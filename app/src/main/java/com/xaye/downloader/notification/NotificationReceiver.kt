package com.xaye.downloader.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.xaye.downloader.utils.NotificationConst
import com.xaye.downloader.utils.NotificationHelper
import com.xaye.downloader.utils.Trace

/**
 * Author xaye
 * @date: 2024/6/17
 */
internal class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == NotificationConst.ACTION_NOTIFICATION_DISMISSED) {
            val dismissedId = intent.extras?.getInt(NotificationConst.KEY_NOTIFICATION_ID)
            if (dismissedId != null) {
                NotificationHelper.addToDismissedNotificationIds(dismissedId)
                //点击通知上面的取消按钮触发
                Trace.i(" NotificationReceiver dismissedId: $dismissedId")
            }
            return
        }

//        val notificationId = intent.getIntExtra(NotificationConst.KEY_NOTIFICATION_ID, 0)
//        val notificationManager =
//            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.cancel(notificationId)


    }
}