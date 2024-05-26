package com.xaye.downloader.core

import android.os.Handler
import android.os.Message
import com.xaye.downloader.DownloadConfig
import com.xaye.downloader.entities.DownloadEntry
import com.xaye.downloader.entities.DownloadStatus
import com.xaye.downloader.utilities.Trace
import java.io.File
import java.util.concurrent.ExecutorService

/**
 * Author xaye
 * @date: 2024-05-26 12:02
 */
class DownloaderTask(
    private val entry: DownloadEntry, //下载条目
    private val handler: Handler, //处理下载任务状态的handler
    private val executor: ExecutorService //负责执行下载任务的线程池
) : ConnectThread.ConnectListener, DownloadThread.DownloadListener {

    /*
    *控制下载状态的变量
    * */
    @Volatile
    private var isPaused: Boolean = false //是否暂停

    @Volatile
    private var isCancelled: Boolean = false //是否取消

    private var connectThread: ConnectThread? = null //负责连接的线程

    private var downloadThreads: Array<DownloadThread?>? = null //负责下载的线程
    private lateinit var downloadStatus: Array<DownloadStatus> //记录每个下载线程的状态
    private var lastStamp: Long = 0 //上次更新UI的时间戳
    private val destFile: File = DownloadConfig.getDownloadFile(entry.url) //下载文件的目标存储路径

    /*
    *暂停下载
    * */
    fun pause() {
        Trace.d("download paused")
        isPaused = true //标记为暂停
        connectThread?.takeIf { it.isRunning }?.cancel() //如果连接线程正在运行，则取消
        downloadThreads?.filterNotNull()?.filter { it.isRunning() }?.let { //如果下载线程正在运行，则暂停或取消
            if (entry.isSupportRange) { //如果服务器支持断点续传，则暂停
                it.forEach { thread -> thread.pause() }
            } else { //否则，取消下载
                it.forEach { thread -> thread.cancel() }
            }
        }
    }

    /*
    *取消下载
    * */
    fun cancel() {
        Trace.d("download cancelled")
        isCancelled = true //标记为取消
        connectThread?.takeIf { it.isRunning }?.cancel() //如果连接线程正在运行，则取消
        downloadThreads?.filterNotNull()?.filter { it.isRunning() }?.forEach { it.cancel() } //如果下载线程正在运行，则取消
    }

    /*
    *开始下载
    * */
    fun start() {
        if (entry.totalLength > 0) { //如果已知文件总长度，则直接开始下载
            startDownload()
        } else { //否则，先连接服务器，获取文件总长度
            entry.status = DownloadStatus.CONNECTING
            notifyUpdate(entry, DownloaderService.NOTIFY_CONNECTING)
            connectThread = ConnectThread(entry.url, this)
            executor.execute(connectThread)
        }
    }

    /*
    *开始下载，分为单线程下载和多线程下载两种情况
    * */
    private fun startDownload() {
        if (entry.isSupportRange) { //如果服务器支持断点续传，则进行多线程下载
            startMultiDownload()
        } else { //否则，进行单线程下载
            startSingleDownload()
        }
    }

    /*
    *通知UI更新下载进度
    * */
    private fun notifyUpdate(entry: DownloadEntry, what: Int) {
        val message = Message.obtain()
        message.what = what
        message.obj = entry
        handler.sendMessage(message)
    }

    /*
    *连接服务器成功后的回调
    * */
    override fun onConnected(isSupportRange: Boolean, totalLength: Int) {
        Trace.d("onConnected isSupportRange = $isSupportRange , totalLength = $totalLength")
        entry.isSupportRange = isSupportRange //是否支持断点续传
        entry.totalLength = totalLength //文件总长度
        startDownload() //开始下载
    }

    /*
    *连接服务器失败后的回调
    * */
    override fun onConnectError(message: String) {
        Trace.d("onConnectError message = $message")
        entry.status = if (isPaused) {
            DownloadStatus.PAUSED
        } else if (isCancelled) {
            DownloadStatus.CANCELLED
        } else {
            DownloadStatus.ERROR
        }
        notifyUpdate(entry, DownloaderService.NOTIFY_ERROR)
    }

    /*
    *开始多线程下载, 并且初始化下载线程状态为下载中
    * */
    private fun startMultiDownload() {
        Trace.d("start multi download")

        // Task initialization
        entry.status = DownloadStatus.DOWNLOADING
        notifyUpdate(entry, DownloaderService.NOTIFY_DOWNLOADING)

        val block = entry.totalLength / DownloadConfig.getMaxDownloadThreads()
        var startPos: Int
        var endPos: Int

        if (entry.ranges.isEmpty()) {
            for (i in 0 until DownloadConfig.getMaxDownloadThreads()) {
                entry.ranges[i] = 0
            }
        }

        downloadThreads = arrayOfNulls(DownloadConfig.getMaxDownloadThreads())
        downloadStatus = Array(DownloadConfig.getMaxDownloadThreads()) { DownloadStatus.DOWNLOADING }

        for (i in 0 until DownloadConfig.getMaxDownloadThreads()) {
            startPos = i * block + entry.ranges[i]!!

            endPos = if (i == DownloadConfig.getMaxDownloadThreads() - 1) {
                entry.totalLength - 1
            } else {
                (i + 1) * block - 1
            }

            if (startPos < endPos) {
                downloadThreads!![i] = DownloadThread(entry.url, destFile, i, startPos, endPos, this)
                executor.execute(downloadThreads!![i])
            } else {
                downloadStatus[i] = DownloadStatus.COMPLETED
            }
        }
    }

    /*
    *开始单线程下载, 并且初始化下载线程状态为下载中
    * */
    private fun startSingleDownload() {
        Trace.e("start single download")

        // Task initialization
        entry.status = DownloadStatus.DOWNLOADING
        notifyUpdate(entry, DownloaderService.NOTIFY_DOWNLOADING)

        downloadThreads = arrayOf(DownloadThread(entry.url, destFile, 0, 0, 0, this))
        executor.execute(downloadThreads!![0])
    }

    /*
    *下载进度改变的回调
    * */
    override fun onProgressChanged(index: Int, progress: Int) {
        synchronized(this) {
            if (entry.isSupportRange) {
                val range = entry.ranges[index]!! + progress
                entry.ranges[index] = range
            }

            entry.currentLength += progress

            val stamp = System.currentTimeMillis()
            if (stamp - lastStamp > 1000) {
                lastStamp = stamp
                notifyUpdate(entry, DownloaderService.NOTIFY_UPDATING)
            }
        }
    }

    /*
    *下载完成的回调
    * */
    override fun onDownloadCompleted(index: Int) {
        synchronized(this) {
            Trace.d("onDownloadCompleted index = $index")

            downloadStatus[index] = DownloadStatus.COMPLETED

            if (downloadStatus.all { it == DownloadStatus.COMPLETED }) {
                Trace.e(" onDownloadCompleted  entry.getCurrentLength() = " + entry.currentLength + " , entry.getTotalLength()" + entry.totalLength)

                if (entry.totalLength > 0 && entry.currentLength != entry.totalLength) {
                    entry.status = DownloadStatus.ERROR
                    entry.reset()
                } else {
                    entry.status = DownloadStatus.COMPLETED
                    notifyUpdate(entry, DownloaderService.NOTIFY_COMPLETED)
                }
            }
        }
    }

    /*
    *下载错误的回调
    * */
    override fun onDownloadError(index: Int, message: String) {
        synchronized(this) {
            Trace.d("onDownloadError message = $message")

            downloadStatus[index] = DownloadStatus.ERROR

            // If one thread encounters an error, cancel all threads
            downloadThreads?.filterNotNull()?.forEach { it.cancelByError() }

            entry.status = DownloadStatus.ERROR
            notifyUpdate(entry, DownloaderService.NOTIFY_ERROR)
        }
    }

    /*
    *下载暂停的回调
    * */
    override fun onDownloadPaused(index: Int) {
        synchronized(this) {
            Trace.d("onDownloadPaused index = $index")

            downloadStatus[index] = DownloadStatus.PAUSED

            if (downloadStatus.all { it == DownloadStatus.PAUSED || it == DownloadStatus.COMPLETED }) {
                entry.status = DownloadStatus.PAUSED
                notifyUpdate(entry, DownloaderService.NOTIFY_PAUSED_OR_CANCELED)
            }
        }
    }

    /*
    *下载取消的回调
    * */
    override fun onDownloadCancelled(index: Int) {
        synchronized(this) {
            Trace.d("onDownloadCancelled index = $index")

            downloadStatus[index] = DownloadStatus.CANCELLED

            if (downloadStatus.all { it == DownloadStatus.CANCELLED || it == DownloadStatus.COMPLETED }) {
                entry.status = DownloadStatus.CANCELLED
                entry.reset()

                notifyUpdate(entry, DownloaderService.NOTIFY_PAUSED_OR_CANCELED)
            }
        }
    }
}
