package com.xaye.downloader.utilities;

import android.util.Log;

/**
 * @FileName:com.xaye.downloader.Trace.kt Author xaye
 * @date: 2024-04-05 09:16
 * Created by 11623 on 2024/4/5
 */
public class Trace {
    public static final String TAG = "Downloader";
    public static final boolean DEBUG = true;

    public static void d(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }

    public static void e(String msg) {
        if (DEBUG) {
            Log.e(TAG, msg);
        }
    }


}
