package com.xaye.downloader.core

import com.xaye.downloader.entities.DownloadStatus
import com.xaye.downloader.utilities.Constants
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL

/**
 * Author xaye
 * @date: 2024-05-26 12:14
 */
class DownloadThread(
    private val url: String,
    private val destFile: File,
    private val index: Int,
    private val startPos: Int,
    private val endPos: Int,
    private val listener: DownloadListener
) : Runnable {

    private var isPaused: Boolean = false // 是否暂停
    private var isCanceled: Boolean = false // 是否取消
    private var isError: Boolean = false // 是否出错
    private var mStates: DownloadStatus = DownloadStatus.IDLE // 当前下载状态
    private val isSingleDownload: Boolean = startPos == 0 && endPos == 0 // 是否为单线程下载

    override fun run() {
        mStates = DownloadStatus.DOWNLOADING
        var connection: HttpURLConnection? = null
        try {
            connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            if (!isSingleDownload) {
                connection.setRequestProperty("Range", "bytes=$startPos-$endPos")
            }
            connection.connectTimeout = Constants.CONNECT_TIMEOUT
            connection.readTimeout = Constants.READ_TIMEOUT
            val responseCode = connection.responseCode

            if (!destFile.parentFile.exists()) {
                destFile.parentFile.mkdirs()
            }

            val raf: RandomAccessFile?
            val fos: FileOutputStream?
            val ins: InputStream?

            if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                raf = RandomAccessFile(destFile, "rw")
                raf.seek(startPos.toLong())
                ins = connection.inputStream
                val buffer = ByteArray(2048)
                var len: Int
                while (ins.read(buffer).also { len = it } != -1) {
                    if (isPaused || isCanceled || isError) {
                        break
                    }
                    raf.write(buffer, 0, len)
                    listener.onProgressChanged(index, len)
                }
                raf.close()
                ins.close()
            } else if (responseCode == HttpURLConnection.HTTP_OK) {
                fos = FileOutputStream(destFile)
                ins = connection.inputStream
                val buffer = ByteArray(2048)
                var len: Int
                while (ins.read(buffer).also { len = it } != -1) {
                    if (isPaused || isCanceled || isError) {
                        break
                    }
                    fos.write(buffer, 0, len)
                    synchronized(listener) {
                        listener.onProgressChanged(index, len)
                    }
                }
                fos.close()
                ins.close()
            } else {
                synchronized(listener) {
                    mStates = DownloadStatus.ERROR
                    listener.onDownloadError(index, "server error:$responseCode")
                }
                return
            }
            synchronized(listener) {
                when {
                    isPaused -> {
                        mStates = DownloadStatus.PAUSED
                        listener.onDownloadPaused(index)
                    }

                    isCanceled -> {
                        mStates = DownloadStatus.CANCELLED
                        listener.onDownloadCancelled(index)
                    }

                    isError -> {
                        mStates = DownloadStatus.ERROR
                        listener.onDownloadError(index, "cancel manually by error")
                    }

                    else -> {
                        mStates = DownloadStatus.COMPLETED
                        listener.onDownloadCompleted(index)
                    }
                }
            }

        } catch (e: IOException) {
            synchronized(listener) {
                when {
                    isPaused -> {
                        mStates = DownloadStatus.PAUSED
                        listener.onDownloadPaused(index)
                    }

                    isCanceled -> {
                        mStates = DownloadStatus.CANCELLED
                        listener.onDownloadCancelled(index)
                    }

                    else -> {
                        mStates = DownloadStatus.ERROR
                        listener.onDownloadError(index, e.message!!)
                    }
                }
            }
        } finally {
            connection?.disconnect()
        }
    }

    fun isRunning(): Boolean = mStates == DownloadStatus.DOWNLOADING

    fun pause() {
        isPaused = true
        Thread.currentThread().interrupt()
    }

    fun isPaused(): Boolean =
        mStates == DownloadStatus.PAUSED || mStates == DownloadStatus.COMPLETED

    fun cancel() {
        isCanceled = true
        Thread.currentThread().interrupt()
    }

    fun isCancelled(): Boolean =
        mStates == DownloadStatus.CANCELLED || mStates == DownloadStatus.COMPLETED

    fun isError(): Boolean = mStates == DownloadStatus.ERROR

    fun cancelByError() {
        isError = true
        Thread.currentThread().interrupt()
    }

    fun isCompleted(): Boolean = mStates == DownloadStatus.COMPLETED

    interface DownloadListener {
        fun onProgressChanged(index: Int, progress: Int)
        fun onDownloadCompleted(index: Int)
        fun onDownloadError(index: Int, message: String)
        fun onDownloadPaused(index: Int)
        fun onDownloadCancelled(index: Int)
    }

}