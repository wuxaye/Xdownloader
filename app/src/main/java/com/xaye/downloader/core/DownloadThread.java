package com.xaye.downloader.core;

import android.os.Environment;

import com.xaye.downloader.utilities.Constants;
import com.xaye.downloader.utilities.Trace;
import com.xaye.downloader.entities.DownloadStatus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @FileName:com.xaye.downloader.DownloaderThread.kt Author xaye
 * @date: 2024-04-05 08:46
 * Created by 11623 on 2024/4/5
 */
public class DownloadThread implements Runnable{
    private final String url;
    private final int startPos;
    private final int endPos;
    private final int index;
    private boolean isSingleDownload;
    private final File destFile;

    private DownloadListener listener;

    private volatile boolean isPaused;

    private volatile boolean isCanceled;

    private volatile boolean isError;
    private DownloadStatus mStates;

    public DownloadThread(String url,File destFile,int index, int startPos, int endPos,DownloadListener listener) {
        this.url = url;
        this.index = index;
        this.startPos = startPos;
        this.endPos = endPos;
        this.destFile = destFile;
        if (startPos == 0 && endPos == 0) {
            isSingleDownload = true;
        }
        this.listener = listener;
    }

    @Override
    public void run() {
        mStates = DownloadStatus.DOWNLOADING;
        HttpURLConnection connection = null;
        try{
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            if (!isSingleDownload) {
                connection.setRequestProperty("Range","bytes="+ startPos + "-" + endPos);
            }
            connection.setConnectTimeout(Constants.CONNECT_TIMEOUT);
            connection.setReadTimeout(Constants.READ_TIMEOUT);
            int responseCode = connection.getResponseCode();
            int contentLength = connection.getContentLength();


            if (!destFile.getParentFile().exists()) {
                destFile.getParentFile().mkdirs(); // 创建文件所在的目录
            }

            RandomAccessFile raf = null;
            FileOutputStream fos = null;
            InputStream is = null;

            if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                raf = new RandomAccessFile(destFile, "rw");
                raf.seek(startPos);
                is = connection.getInputStream();
                byte[] buffer = new byte[2048];
                int len = -1;
                while ((len = is.read(buffer)) != -1) {
                    if (isPaused || isCanceled || isError) {
                        Trace.d(" test cancel isPaused = "+isPaused +" , isCanceled = "+isCanceled);
                        break;
                    }
                    raf.write(buffer, 0, len);
                    listener.onProgressChanged(index,len);
                }
                raf.close();
                is.close();
            } else if(responseCode == HttpURLConnection.HTTP_OK){
                fos = new FileOutputStream(destFile);
                is = connection.getInputStream();
                byte[] buffer = new byte[2048];
                int len = -1;
                while ((len = is.read(buffer)) != -1) {
                    if (isPaused || isCanceled || isError) {
                        break;
                    }
                    fos.write(buffer, 0, len);
                    synchronized (listener) {
                        listener.onProgressChanged(index,len);
                    }
                }
                fos.close();
                is.close();
            } else {
                synchronized (listener) {
                    mStates = DownloadStatus.ERROR;
                    listener.onDownloadError(index,"server error:"+responseCode);
                }
                return;
            }
            synchronized (listener) {
                if (isPaused) {
                    mStates = DownloadStatus.PAUSED;
                    listener.onDownloadPaused(index);
                } else if (isCanceled) {
                    Trace.d("test cancel 000 --->");
                    mStates = DownloadStatus.CANCELLED;
                    listener.onDownloadCancelled(index);
                } else if (isError) {
                    mStates = DownloadStatus.ERROR;
                    listener.onDownloadError(index,"cancel manually by error");
                } else {
                    mStates = DownloadStatus.COMPLETED;
                    listener.onDownloadCompleted(index);
                }
            }

        }catch (IOException e) {
            synchronized (listener) {
                if (isPaused) {
                    mStates = DownloadStatus.PAUSED;
                    listener.onDownloadPaused(index);
                } else if (isCanceled) {
                    mStates = DownloadStatus.CANCELLED;
                    listener.onDownloadCancelled(index);
                }else {
                    mStates = DownloadStatus.ERROR;
                    listener.onDownloadError(index,e.getMessage());
                }
            }

            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public boolean isRunning() {
        return mStates == DownloadStatus.DOWNLOADING;
    }

    public void pause() {
        isPaused = true;
        Thread.currentThread().interrupt();
    }

    public boolean isPaused() {
        return mStates == DownloadStatus.PAUSED || mStates == DownloadStatus.COMPLETED;
    }

    public void cancel() {
        isCanceled = true;
        Thread.currentThread().interrupt();
    }

    public boolean isCancelled() {
        return mStates == DownloadStatus.CANCELLED || mStates == DownloadStatus.COMPLETED;
    }

    public boolean isError() {
        return mStates == DownloadStatus.ERROR;
    }

    public void cancelByError() {
        isError = true;
        Thread.currentThread().interrupt();
    }

    public boolean isCompleted() {
        return mStates == DownloadStatus.COMPLETED;
    }


    interface DownloadListener {
        void onProgressChanged(int index,int progress);
        void onDownloadCompleted(int index);
        void onDownloadError(int index,String message);
        void onDownloadPaused(int index);

        void onDownloadCancelled(int index);
    }

}
