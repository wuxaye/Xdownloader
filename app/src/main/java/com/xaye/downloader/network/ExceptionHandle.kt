package com.xaye.downloader.network

import android.net.ParseException
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import org.apache.http.conn.ConnectTimeoutException
import org.json.JSONException
import java.net.ConnectException
import javax.net.ssl.SSLHandshakeException

/**
 * Author xaye
 * @date: 2024/5/31
 */
object ExceptionHandle {
    fun handleException(e: Throwable?): DownloadException {
        val ex: DownloadException
        e?.let {
            when (it) {
                is JsonParseException, is JSONException, is ParseException, is MalformedJsonException -> {
                    ex = DownloadException(Error.PARSE_ERROR, e)
                    return ex
                }
                is ConnectException , is SSLHandshakeException -> {
                    ex = DownloadException(Error.NETWORK_ERROR, e)
                    return ex
                }
                is javax.net.ssl.SSLException -> {
                    ex = DownloadException(Error.SSL_ERROR, e)
                    return ex
                }
                is ConnectTimeoutException -> {
                    ex = DownloadException(Error.TIMEOUT_ERROR, e)
                    return ex
                }
                is java.net.SocketTimeoutException -> {
                    ex = DownloadException(Error.TIMEOUT_ERROR, e)
                    return ex
                }
                is java.net.UnknownHostException -> {
                    ex = DownloadException(Error.TIMEOUT_ERROR, e)
                    return ex
                }
                is DownloadException -> return it

                else -> {
                    ex = DownloadException(Error.UNKNOWN, e)
                    return ex
                }
            }
        }
        ex = DownloadException(Error.UNKNOWN, e)
        return ex
    }
}