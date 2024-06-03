package com.xaye.downloader.core

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import com.xaye.downloader.DownloadConfig
import com.xaye.downloader.DownloadConfig.getMaxDownloadTasks
import com.xaye.downloader.DownloadConfig.getRecoverDownloadWhenStart
import com.xaye.downloader.db.DownloadDatabase
import com.xaye.downloader.entities.DownloadEntry
import com.xaye.downloader.entities.DownloadStatus
import com.xaye.downloader.notify.DataChanger
import com.xaye.downloader.utilities.Constants
import com.xaye.downloader.utilities.Trace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

/**
 * Author xaye
 * @date: 2024-05-26 11:05
 */
class DownloaderService : Service() {

    //定义常量
    companion object {
        const val NOTIFY_DOWNLOADING = 0x101
        const val NOTIFY_COMPLETED = 0x102
        const val NOTIFY_UPDATING = 0x103
        const val NOTIFY_PAUSED_OR_CANCELED = 0x104
        const val NOTIFY_CONNECTING = 0x105
        const val NOTIFY_ERROR = 0x106
    }

    //初始化变量
    private val mDownloadingTasks = mutableMapOf<String, DownloaderTask2>()
    private lateinit var mExecutors: ExecutorService
    private lateinit var mDataChanger: DataChanger
    private lateinit var mDatabase: DownloadDatabase
    private val mWaitingQueue = LinkedBlockingQueue<DownloadEntry>()
    private val mHandler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                NOTIFY_PAUSED_OR_CANCELED, NOTIFY_COMPLETED, NOTIFY_ERROR -> checkNext(msg.obj as DownloadEntry)
            }
            mDataChanger.postStatus(msg.obj as DownloadEntry)
        }
    }

    //检查下一个任务
    private fun checkNext(entry: DownloadEntry) {
        mDownloadingTasks.remove(entry.key)
        // if is not empty, poll next and start download
        mWaitingQueue.poll()?.let { startDownload(it) }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mExecutors = Executors.newCachedThreadPool()
        mDataChanger = DataChanger.getInstance(applicationContext)
        mDatabase = DownloadDatabase.getInstance(applicationContext)
        initializeDownload()
    }

    //初始化下载任务
    private fun initializeDownload() {
        CoroutineScope(Dispatchers.IO).launch {
            val entries = mDatabase.downloadEntryDao().getAllDownloads()
            entries.forEach { entry ->
                handleEntry(entry)
                mDataChanger.addToOperatedEntryMap(entry.key, entry)
            }
        }
    }

    // 处理每个条目的函数
    private suspend fun handleEntry(entry: DownloadEntry) {
        if (entry.status == DownloadStatus.DOWNLOADING || entry.status == DownloadStatus.WAITING) {
            if (getRecoverDownloadWhenStart()) {
                updateEntryForRecovery(entry)
                addDownload(entry)
            } else {
                updateEntryForNonRecovery(entry)
                mDatabase.downloadEntryDao().insertOrUpdate(entry)
            }
        }
    }

    // 恢复下载的更新操作
    private fun updateEntryForRecovery(entry: DownloadEntry) {
        if (entry.isSupportRange) {
            entry.status = DownloadStatus.PAUSED
        } else {
            entry.status = DownloadStatus.IDLE
            entry.reset()
        }
    }

    // 非恢复下载的更新操作
    private fun updateEntryForNonRecovery(entry: DownloadEntry) {
        if (entry.isSupportRange) {
            entry.status = DownloadStatus.PAUSED
        } else {
            entry.status = DownloadStatus.IDLE
            entry.reset()
        }
    }

    //处理Intent指令
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val entry = it.getParcelableExtra<DownloadEntry>(Constants.KEY_DOWNLOAD_ENTRY)
                ?.let { downloadEntry ->
                    if (mDataChanger.containsDownloadEntry(downloadEntry.key)) {
                        mDataChanger.queryDownloadEntryById(downloadEntry.key)
                    } else {
                        downloadEntry
                    }
                }

            val action = it.getIntExtra(Constants.KEY_DOWNLOAD_ACTION, -1)
            doAction(entry, action)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    //根据指令执行对应的操作
    private fun doAction(entry: DownloadEntry?, action: Int) {
        when (action) {
            Constants.KEY_DOWNLOAD_ACTION_ADD -> addDownload(entry!!)
            Constants.KEY_DOWNLOAD_ACTION_PAUSE -> pauseDownload(entry!!)
            Constants.KEY_DOWNLOAD_ACTION_RESUME -> resumeDownload(entry!!)
            Constants.KEY_DOWNLOAD_ACTION_CANCEL -> cancelDownload(entry!!)
            Constants.KEY_DOWNLOAD_ACTION_PAUSE_ALL -> pauseAll()
            Constants.KEY_DOWNLOAD_ACTION_RECOVER_ALL -> recoverAll()
        }
    }

    //恢复所有下载
    private fun recoverAll() {
        val mRecoverableEntries =
            DataChanger.getInstance(applicationContext).queryAllRecoverableEntries()
        mRecoverableEntries.let {
            for (downloadEntry in it) {
                addDownload(downloadEntry)
            }
        }
    }

    //暂停所有下载
    private fun pauseAll() {
        while (mWaitingQueue.iterator().hasNext()) {
            val next = mWaitingQueue.poll()
            next?.let {
                it.status = DownloadStatus.PAUSED
                DataChanger.getInstance(applicationContext).postStatus(it)
            }
        }

        for (entry in mDownloadingTasks) {
            entry.value.pause()
        }

        mDownloadingTasks.clear()
    }

    //添加下载任务
    private fun addDownload(entry: DownloadEntry) {
        if (mDownloadingTasks.size >= getMaxDownloadTasks()) {
            mWaitingQueue.offer(entry)
            entry.status = DownloadStatus.WAITING
            DataChanger.getInstance(applicationContext).postStatus(entry)
        } else {
            startDownload(entry)
        }
    }

    //取消下载任务
    private fun cancelDownload(entry: DownloadEntry) {
        mDownloadingTasks.remove(entry.key)?.cancel()
        mWaitingQueue.remove(entry)
        entry.status = DownloadStatus.CANCELLED
        DataChanger.getInstance(applicationContext).postStatus(entry)
    }

    //继续下载任务
    private fun resumeDownload(entry: DownloadEntry) {
        addDownload(entry)
    }

    //暂停下载任务
    private fun pauseDownload(entry: DownloadEntry) {
        mDownloadingTasks.remove(entry.key)?.pause()
        mWaitingQueue.remove(entry)
        entry.status = DownloadStatus.PAUSED
        DataChanger.getInstance(applicationContext).postStatus(entry)
    }

    //开始下载任务
    private fun startDownload(entry: DownloadEntry) {
        //FIXME 切换网络3g 没有内存 ，没有sd卡 等情况，自行实现
        val task = DownloaderTask2(entry, mHandler, mExecutors,applicationContext)
        task.start()
        mDownloadingTasks[entry.key] = task
    }

}