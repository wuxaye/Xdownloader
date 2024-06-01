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

    /**
     * 初始化
     */
    fun init(context: Context) {
        this.context = context.applicationContext
        context.startService(Intent(context, DownloaderService::class.java))
    }

    /**
     * 添加下载任务
     */
    fun add(entry: DownloadEntry) {
        val intent = Intent(context, DownloaderService::class.java).apply {
            putExtra(Constants.KEY_DOWNLOAD_ENTRY, entry)
            putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_ADD)
        }
        context.startService(intent)
    }

    /**
     * 暂停单个任务下载
     */
    fun pause(entry: DownloadEntry) {
        val intent = Intent(context, DownloaderService::class.java).apply {
            putExtra(Constants.KEY_DOWNLOAD_ENTRY, entry)
            putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_PAUSE)
        }
        context.startService(intent)
    }

    /**
     * 暂停所有下载任务
     */
    fun pauseAll() {
        val intent = Intent(context, DownloaderService::class.java).apply {
            putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_PAUSE_ALL)
        }
        context.startService(intent)
    }

    /**
     * 恢复所有下载任务
     */
    fun recoverAll() {
        val intent = Intent(context, DownloaderService::class.java).apply {
            putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_RECOVER_ALL)
        }
        context.startService(intent)
    }

    /**
     * 恢复下载
     * 支持断点续传：从上次断点继续下载
     * 不支持断点续传：从头开始下载
     */
    fun resume(entry: DownloadEntry) {
        val intent = Intent(context, DownloaderService::class.java).apply {
            putExtra(Constants.KEY_DOWNLOAD_ENTRY, entry)
            putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_RESUME)
        }
        context.startService(intent)
    }

    /**
     * 取消下载
     * 取消所有下载线程并删除文件
     */
    fun cancel(entry: DownloadEntry) {
        val intent = Intent(context, DownloaderService::class.java).apply {
            putExtra(Constants.KEY_DOWNLOAD_ENTRY, entry)
            putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_CANCEL)
        }
        context.startService(intent)
    }

    /**
     * 获取下载状态
     */
    fun getObserver(): LiveData<DownloadEntry> {
        return DataChanger.getInstance(context).entriesLiveData
    }

    fun download(entry: DownloadEntry): LiveData<DownloadEntry>? {
        val intent = Intent(context, DownloaderService::class.java).apply {
            putExtra(Constants.KEY_DOWNLOAD_ENTRY, entry)
            putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_ADD)
        }
        context.startService(intent)


        return null
    }

    fun queryDownloadEntry(id: String): DownloadEntry? {
        return DataChanger.getInstance(context).queryDownloadEntryById(id)
    }

    /**
     * 删除下载任务，并删除文件
     */
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