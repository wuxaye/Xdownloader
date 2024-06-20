package com.xaye.downloader.utils

/**
 * Author xaye
 * @date: 2024/6/17
 */
internal object NotificationHelper {

    private val dismissedNotificationIds = mutableListOf<Int>()

    fun addToDismissedNotificationIds(id: Int) {
        dismissedNotificationIds.add(id)
    }

    fun isDismissedNotification(id: Int?): Boolean {
        return id in dismissedNotificationIds
    }
}