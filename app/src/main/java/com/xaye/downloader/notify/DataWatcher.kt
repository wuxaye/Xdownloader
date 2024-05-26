package com.xaye.downloader.notify

import com.xaye.downloader.entities.DownloadEntry
import java.util.Observable
import java.util.Observer

/**
 * Author xaye
 * @date: 2024-05-26 19:03
 */
interface DataWatcher : Observer {
    override fun update(o: Observable?, arg: Any?) {
        if (arg is DownloadEntry) {
            notifyUpdate(arg)
        }
    }

    fun notifyUpdate(arg: DownloadEntry)
}