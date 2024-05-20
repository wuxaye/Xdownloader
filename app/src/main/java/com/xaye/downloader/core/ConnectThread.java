package com.xaye.downloader.core;

import com.xaye.downloader.utilities.Constants;
import com.xaye.downloader.utilities.Trace;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @FileName:com.xaye.downloader.ConnectThread.kt Author xaye
 * @date: 2024-05-04 10:12
 * Created by 11623 on 2024/5/4
 */
public class ConnectThread implements Runnable{

    private final String url;
    private final ConnectListener listener;

    private volatile boolean isRunning;

    public ConnectThread(String url, ConnectListener listener) {
        this.url = url;
        this.listener = listener;
    }

    @Override
    public void run() {
        isRunning = true;
        HttpURLConnection connection = null;
        try{
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            //connection.setRequestProperty("Range","bytes=0-"+Integer.MAX_VALUE);
            connection.setConnectTimeout(Constants.CONNECT_TIMEOUT);
            connection.setReadTimeout(Constants.READ_TIMEOUT);
            int responseCode = connection.getResponseCode();
            int contentLength = connection.getContentLength();
            boolean isSupportRange = false;

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String ranges = connection.getHeaderField("Accept-Ranges");
                if (ranges != null && ranges.equals("bytes")) {
                    isSupportRange = true;
                }
                listener.onConnected(isSupportRange,contentLength);

            } else {
                listener.onConnectError("server error responseCode:"+responseCode);
            }

            Trace.d("ConnectThread responseCode:"+responseCode);

            isRunning = false;
        }catch (IOException e) {
            isRunning = false;
            listener.onConnectError(e.getMessage());
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void cancel() {
        Thread.currentThread().interrupt();
    }

    interface ConnectListener{
        void onConnected(boolean isSupportRange, int totalLength);

        void onConnectError(String message);
    }
}
