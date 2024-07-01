package com.xaye.downloader

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import com.xaye.downloader.core.DownloaderService
import com.xaye.downloader.entities.DownloadEntry
import com.xaye.downloader.entities.DownloadStatus
import com.xaye.downloader.listener.DownLoadListener
import com.xaye.downloader.notify.DataChanger
import com.xaye.downloader.utils.Constants
import com.xaye.downloader.utils.Trace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

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
     * 添加下载任务,直接开始下载
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
    fun getObserverLiveData(): LiveData<DownloadEntry> {
        return DataChanger.getInstance(context).entriesLiveData
    }

    fun getObserverFlow(): SharedFlow<DownloadEntry> {
        return DataChanger.getInstance(context).downloadStatus
    }

    /**
     * 单文件下载及监听
     */
    fun download(
        tag: String,
        url: String,
        savePath: String? = null,//文件路径 可自定义，默认使用 FileUtils.getDownloadDir()
        saveName: String? = null,//文件名 可自定义，默认使用 FileUtils.getMd5FileName(url)
        reDownload: Boolean = false,//是否强制重新下载（无论文件下载完成没完成，都重新下载）
        listener: DownLoadListener?
    ): DownloadEntry {
        val entry = DownloadEntry(
            key = tag,
            fileName = tag,
            url = url,
            path = if (!savePath.isNullOrEmpty() && !saveName.isNullOrEmpty()) {
                File(savePath, saveName).absolutePath
            } else {
                DownloadConfig.getDefaultDownloadFile(url).absolutePath
            },
            reDownload = reDownload
        )

        val intent = Intent(context, DownloaderService::class.java).apply {
            putExtra(Constants.KEY_DOWNLOAD_ENTRY, entry)
            putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_ADD)
        }
        if (reDownload) {
            DataChanger.getInstance(context).deleteDownloadEntry(entry.key)
        }
        context.startService(intent)

        //也可以外部多个地方收集
        CoroutineScope(Dispatchers.Main).launch {
            getObserverFlow().collectLatest { entry ->
                if (entry.key == tag) {
                    Trace.d("collectLatest DownloadManager.download() entry = $entry")
                    listener?.let { entry.notifyListener(listener) }

                    // 检查下载是否完成，如果完成则取消收集器
                    if (entry.status == DownloadStatus.COMPLETED || entry.status == DownloadStatus.ERROR) {
                        this.cancel()// 取消收集器.
                    }
                }
            }
        }
        return entry
    }

    fun queryDownloadEntry(id: String): DownloadEntry? {
        return DataChanger.getInstance(context).queryDownloadEntryById(id)
    }

    /**
     * 删除下载任务，并删除文件
     */
//    fun deleteDownloadEntry(forceDelete: Boolean, id: String) {
//        DataChanger.getInstance(context).deleteDownloadEntry(id)
//        // TODO: 删除文件
//        if (forceDelete) {
//            val file = DownloadConfig.getDefaultDownloadFile(id)
//            if (file.exists()) {
//                file.delete()
//            }
//        }
//    }
}