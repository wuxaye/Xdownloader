package com.xaye.downloader.core;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.xaye.downloader.DownloadConfig;
import com.xaye.downloader.utilities.Constants;
import com.xaye.downloader.utilities.Trace;
import com.xaye.downloader.entities.DownloadEntry;
import com.xaye.downloader.entities.DownloadStatus;

import java.io.File;
import java.util.concurrent.ExecutorService;

/**
 * @FileName:com.xaye.downloader.DownloaderTask.kt Author xaye
 * @date: 2024-04-05 08:46
 * Created by 11623 on 2024/4/5
 *
 * 1.check if support range, get content-length
 * 2.if not, single thread to download, can't be paused|resumed
 * 3.if support, multi thread to download
 * 3.1 compute the block size per thread
 * 3.2 execute sub-threads
 * 3.3 combine the progress and notify
 */
public class DownloaderTask implements  ConnectThread.ConnectListener, DownloadThread.DownloadListener {
    private final DownloadEntry entry;
    private final Handler mHandler;
    private final ExecutorService mExecutor;
    private volatile boolean isPaused;
    private volatile boolean isCancelled;

    private ConnectThread mConnectThread;

    private DownloadThread[] mDownloadThreads;
    private DownloadStatus[] mDownloadStatus;
    private long mLastStamp = 0;
    private File destFile;

    public DownloaderTask(DownloadEntry entry, Handler handler, ExecutorService mExecutor) {
        this.entry = entry;
        this.mHandler = handler;
        this.mExecutor = mExecutor;
        this.destFile = DownloadConfig.getDownloadFile(entry.getUrl());

        Trace.d("DownloaderTask  destFile = "+destFile.getAbsolutePath());
    }

    public void pause() {
        Trace.e("download paused");
        isPaused = true;
        if (mConnectThread != null && mConnectThread.isRunning()) {
            mConnectThread.cancel();
        }

        if (mDownloadThreads != null && mDownloadThreads.length > 0) {
            for (DownloadThread downloadThread : mDownloadThreads) {
                if (downloadThread != null && downloadThread.isRunning()) {
                    if (entry.isSupportRange()) {
                        downloadThread.pause();
                    } else {
                        downloadThread.cancel();
                    }
                }
            }
        }
    }

    public void cancel() {
        Trace.e("download cancelled");
        isCancelled = true;
        if (mConnectThread != null && mConnectThread.isRunning()) {
            mConnectThread.cancel();
        }

        if (mDownloadThreads != null && mDownloadThreads.length > 0) {
            for (DownloadThread downloadThread : mDownloadThreads) {
                if (downloadThread != null && downloadThread.isRunning()) {
                    downloadThread.cancel();
                }
            }
        }
    }

    public void start() {

        if (entry.getTotalLength() > 0) {//已经连接判断过了，不需要再判断
            startDownload();
        } else {
            entry.setStatus(DownloadStatus.CONNECTING);
            notifyUpdate(entry,DownloaderService.NOTIFY_CONNECTING);
            mConnectThread = new ConnectThread(entry.getUrl(),this);
            mExecutor.execute(mConnectThread);
        }


    }

    private void startDownload() {

        if (entry.isSupportRange()) {
            // TODO: multi threads
            startMultiDownload();
        } else {
            // TODO: single thread
            startSingleDownload();
        }

        //mCountDownLatch = new CountDownLatch(mDownloadThreads.length);
    }

    private void notifyUpdate(DownloadEntry entry, int what) {
        Message message = Message.obtain();
        message.what = what;
        message.obj = entry;
        mHandler.sendMessage(message);
    }

    @Override
    public void onConnected(boolean isSupportRange, int totalLength) {
        Trace.d("onConnected isSupportRange = "+isSupportRange+" , totalLength = "+totalLength);
        entry.setSupportRange(isSupportRange);
        entry.setTotalLength(totalLength);

        startDownload();
    }

    @Override
    public void onConnectError(String message) {
        Trace.e("onConnectError message = "+message);
        if (isPaused || isCancelled) {
            entry.setStatus(isPaused?DownloadStatus.PAUSED:DownloadStatus.CANCELLED);
            notifyUpdate(entry,DownloaderService.NOTIFY_PAUSED_OR_CANCELED);
        } else {
            entry.setStatus(DownloadStatus.ERROR);
            notifyUpdate(entry,DownloaderService.NOTIFY_ERROR);
        }
    }

    private void startMultiDownload() {
        Trace.e("start multi download");
        entry.setStatus(DownloadStatus.DOWNLOADING);
        notifyUpdate(entry,DownloaderService.NOTIFY_DOWNLOADING);

        int block = entry.getTotalLength() / DownloadConfig.getMaxDownloadThreads();
        int startPos = 0;
        int endPos = 0;

        if (entry.getRanges().isEmpty()) {
            for (int i = 0; i < DownloadConfig.getMaxDownloadThreads(); i++) {
                entry.getRanges().put(i,0);
            }
        }
        mDownloadThreads = new DownloadThread[DownloadConfig.getMaxDownloadThreads()];
        mDownloadStatus = new DownloadStatus[DownloadConfig.getMaxDownloadThreads()];

        for (int i = 0; i < DownloadConfig.getMaxDownloadThreads(); i++) {// 9 .  0 - 2 3 - 5 6 - 8
            startPos = i * block + entry.getRanges().get(i);//10
            if (i == DownloadConfig.getMaxDownloadThreads() - 1) {
                endPos = entry.getTotalLength() - 1;
            } else {
                endPos = (i + 1) * block - 1;
            }

            if (startPos < endPos) {
                mDownloadThreads[i] = new DownloadThread(entry.getUrl(),destFile,i, startPos, endPos,this);
                mDownloadStatus[i] = DownloadStatus.DOWNLOADING;
                mExecutor.execute(mDownloadThreads[i]);
            } else {
                mDownloadStatus[i] = DownloadStatus.COMPLETED;
            }

        }
    }

    private void startSingleDownload() {
        Trace.e("start single download");
        entry.setStatus(DownloadStatus.DOWNLOADING);
        notifyUpdate(entry,DownloaderService.NOTIFY_DOWNLOADING);

        mDownloadThreads = new DownloadThread[1];
        mDownloadThreads[0] = new DownloadThread(entry.getUrl(),destFile,0, 0, 0,this);
        mExecutor.execute(mDownloadThreads[0]);
    }

    @Override
    public synchronized void onProgressChanged(int index,int progress) {
        if (entry.isSupportRange()) {
            int range = entry.getRanges().get(index) + progress;
            entry.getRanges().put(index, range);
        }

            entry.setCurrentLength(entry.getCurrentLength() + progress);

        long stamp = System.currentTimeMillis();
        if (stamp - mLastStamp > 1000) {
            mLastStamp = stamp;
            notifyUpdate(entry,DownloaderService.NOTIFY_UPDATING);
        }
    }

    @Override
    public synchronized void onDownloadCompleted(int index) {
        Trace.d("onDownloadCompleted index = "+index);

        mDownloadStatus[index] = DownloadStatus.COMPLETED;

        for (int i = 0; i < mDownloadStatus.length; i++) {
            if (mDownloadStatus[i] != DownloadStatus.COMPLETED) {
                return;
            }
        }

        Trace.e(" onDownloadCompleted  entry.getCurrentLength() = "+entry.getCurrentLength()+" , entry.getTotalLength()"+entry.getTotalLength());

            //异常情况，直接删除文件
            if (entry.getTotalLength() > 0 && entry.getCurrentLength() != entry.getTotalLength()) {
                entry.setStatus(DownloadStatus.ERROR);
                entry.reset();
            } else {
                entry.setStatus(DownloadStatus.COMPLETED);
                notifyUpdate(entry,DownloaderService.NOTIFY_COMPLETED);
            }

    }

    @Override
    public synchronized void onDownloadError(int index,String message) {
        Trace.d("onDownloadError message = "+message);
        mDownloadStatus[index] = DownloadStatus.ERROR;

        for (int i = 0; i < mDownloadStatus.length; i++) {
            if (mDownloadStatus[i] != DownloadStatus.COMPLETED && mDownloadStatus[i] != DownloadStatus.ERROR) {
                mDownloadThreads[i].cancelByError();
                return;
            }
        }

//        boolean isAllError = true;
//        for (int i = 0; i < mDownloadThreads.length; i++) {
//            if (mDownloadThreads[i] != null) {
//                if (!mDownloadThreads[i].isError()) {
//                    isAllError = false;
//                    Trace.e("cancel download thread "+i+" manually cause net error:"+message);
//                    mDownloadThreads[i].cancelByError();
//                }
//            }
//        }
//        if (isAllError) {
            entry.setStatus(DownloadStatus.ERROR);
            notifyUpdate(entry,DownloaderService.NOTIFY_ERROR);
     //   }
    }

    @Override
    public synchronized void onDownloadPaused(int index) {
        Trace.d("onDownloadPaused index = "+index);

        mDownloadStatus[index] = DownloadStatus.PAUSED;

        for (int i = 0; i < mDownloadStatus.length; i++) {
            if (mDownloadStatus[i] != DownloadStatus.COMPLETED && mDownloadStatus[i] != DownloadStatus.PAUSED) {
                return;
            }
        }

//        for (int i = 0; i < mDownloadThreads.length; i++) {
//            if (mDownloadThreads[i] != null) {
//                if (!mDownloadThreads[i].isPaused()) {
//                    return;
//                }
//            }
//        }
        //当所有的线程都暂停后
        entry.setStatus(DownloadStatus.PAUSED);
        notifyUpdate(entry,DownloaderService.NOTIFY_PAUSED_OR_CANCELED);
    }

    @Override
    public synchronized void onDownloadCancelled(int index) {
        Trace.d("onDownloadCancelled index = "+index);

        mDownloadStatus[index] = DownloadStatus.CANCELLED;

        for (int i = 0; i < mDownloadStatus.length; i++) {
            if (mDownloadStatus[i] != DownloadStatus.COMPLETED && mDownloadStatus[i] != DownloadStatus.CANCELLED) {
                return;
            }
        }


        for (int i = 0; i < mDownloadThreads.length; i++) {
            if (mDownloadThreads[i] != null) {
                if (!mDownloadThreads[i].isCancelled()) {
                    return;
                }
            }
        }
        //当所有的线程都暂停后
        entry.setStatus(DownloadStatus.CANCELLED);
        entry.reset();

        notifyUpdate(entry,DownloaderService.NOTIFY_PAUSED_OR_CANCELED);
    }

}
