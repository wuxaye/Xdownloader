package com.xaye.downloader

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.xaye.downloader.core.DownloaderService
import com.xaye.downloader.entities.DownloadEntry
import com.xaye.downloader.notify.DataChanger
import com.xaye.downloader.utilities.Constants

/**
 * Author xaye
 * @date: 2024-05-26 19:14
 */
@SuppressLint("StaticFieldLeak")
object DownloaderManager {

    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context.applicationContext
        context.startService(Intent(context, DownloaderService::class.java))
    }

    fun add(entry: DownloadEntry) {
        val intent = Intent(context, DownloaderService::class.java).apply {
            putExtra(Constants.KEY_DOWNLOAD_ENTRY, entry)
            putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_ADD)
        }
        context.startService(intent)
    }

    fun pause(entry: DownloadEntry) {
        val intent = Intent(context, DownloaderService::class.java).apply {
            putExtra(Constants.KEY_DOWNLOAD_ENTRY, entry)
            putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_PAUSE)
        }
        context.startService(intent)
    }

    fun pauseAll() {
        val intent = Intent(context, DownloaderService::class.java).apply {
            putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_PAUSE_ALL)
        }
        context.startService(intent)
    }

    fun recoverAll() {
        val intent = Intent(context, DownloaderService::class.java).apply {
            putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_RECOVER_ALL)
        }
        context.startService(intent)
    }

    fun resume(entry: DownloadEntry) {
        val intent = Intent(context, DownloaderService::class.java).apply {
            putExtra(Constants.KEY_DOWNLOAD_ENTRY, entry)
            putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_RESUME)
        }
        context.startService(intent)
    }

    fun cancel(entry: DownloadEntry) {
        val intent = Intent(context, DownloaderService::class.java).apply {
            putExtra(Constants.KEY_DOWNLOAD_ENTRY, entry)
            putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_CANCEL)
        }
        context.startService(intent)
    }

    fun getObserver(): LiveData<DownloadEntry> {
        return DataChanger.getInstance(context).entriesLiveData
    }

//    fun addObserver(dataWatcher: DataWatcher) {
//        DataChanger.getInstance(context).addObserver(dataWatcher)
//    }
//
//    fun deleteObserver(dataWatcher: DataWatcher) {
//        DataChanger.getInstance(context).deleteObserver(dataWatcher)
//    }

    fun queryDownloadEntry(id: String): DownloadEntry? {
        return DataChanger.getInstance(context).queryDownloadEntryById(id)
    }

    fun deleteDownloadEntry(forceDelete: Boolean, id: String) {
        DataChanger.getInstance(context).deleteDownloadEntry(id)
        // TODO: 删除文件
        if (forceDelete) {
            val file = DownloadConfig.getDownloadFile(id)
            if (file.exists()) {
                file.delete()
            }
        }
    }
}