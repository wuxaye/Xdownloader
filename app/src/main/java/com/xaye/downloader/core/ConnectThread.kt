package com.xaye.downloader.core

import com.xaye.downloader.network.DownloadException
import com.xaye.downloader.network.Error
import com.xaye.downloader.network.ExceptionHandle
import com.xaye.downloader.utils.Constants
import com.xaye.downloader.utils.Trace
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class ConnectThread(private val url: String, private val listener: ConnectListener) : Runnable {

    @Volatile
    var isRunning = false
        private set

    override fun run() {
        isRunning = true
        var connection: HttpURLConnection? = null
        try {
            connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = Constants.CONNECT_TIMEOUT
            connection.readTimeout = Constants.READ_TIMEOUT
            val responseCode = connection.responseCode
            val contentLength = connection.contentLength
            var isSupportRange = false

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val ranges = connection.getHeaderField("Accept-Ranges")
                if (ranges != null && ranges == "bytes") {
                    isSupportRange = true
                }
                listener.onConnected(isSupportRange, contentLength)
            } else {
                listener.onConnectError(
                    DownloadException(
                        Error.RESPONSE_CODE_ERROR.getKey(),
                        Error.RESPONSE_CODE_ERROR.getValue(),
                        responseCode.toString(),
                        null
                    )
                )
            }

            Trace.d("ConnectThread responseCode:$responseCode")

            isRunning = false
        } catch (e: IOException) {
            isRunning = false
            Trace.e("ConnectThread error:${e.message}  e: $e")
            listener.onConnectError(ExceptionHandle.handleException(e))
            e.printStackTrace()
        } finally {
            connection?.disconnect()
        }
    }

    fun cancel() {
        Thread.currentThread().interrupt()
    }

    interface ConnectListener {
        fun onConnected(isSupportRange: Boolean, totalLength: Int)

        fun onConnectError(exception: DownloadException)
    }
}
