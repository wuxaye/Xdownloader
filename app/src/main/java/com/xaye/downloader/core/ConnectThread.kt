package com.xaye.downloader.core

import com.xaye.downloader.utilities.Constants
import com.xaye.downloader.utilities.Trace
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
                listener.onConnectError("server error responseCode:$responseCode")
            }

            Trace.d("com.xaye.downloader.core.ConnectThread responseCode:$responseCode")

            isRunning = false
        } catch (e: IOException) {
            isRunning = false
            listener.onConnectError(e.message ?: "")
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

        fun onConnectError(message: String)
    }
}
