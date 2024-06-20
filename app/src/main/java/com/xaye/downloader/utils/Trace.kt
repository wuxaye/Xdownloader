package com.xaye.downloader.utils

import android.util.Log

/**
 * Author xaye
 * @date: 2024-05-26 19:10
 */
object Trace {
    private const val TAG = "Downloader"
    private const val DEBUG = true


    fun d(msg: String) {
        if (DEBUG) {
            Log.d(TAG, msg)
        }
    }

    fun i(msg: String) {
        if (DEBUG) {
            Log.i(TAG, msg)
        }
    }

    fun e(msg: String) {
        if (DEBUG) {
            Log.e(TAG, msg)
        }
    }
}