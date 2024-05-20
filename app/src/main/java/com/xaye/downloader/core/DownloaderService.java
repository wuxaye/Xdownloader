package com.xaye.downloader.core;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.xaye.downloader.DownloadConfig;
import com.xaye.downloader.utilities.Constants;
import com.xaye.downloader.notify.DataChanger;
import com.xaye.downloader.utilities.Trace;
import com.xaye.downloader.db.DownloadDatabase;
import com.xaye.downloader.entities.DownloadEntry;
import com.xaye.downloader.entities.DownloadStatus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @FileName:com.xaye.downloader.DownloaderService.kt Author xaye
 * @date: 2024-04-05 08:45
 * Created by 11623 on 2024/4/5
 */
public class DownloaderService extends Service {

    private HashMap<String, DownloaderTask> mDownloadingTasks = new HashMap<>();

    private ExecutorService mExecutors;

    private DataChanger mDataChanger;

    private DownloadDatabase mDatabase;

    private LinkedBlockingQueue<DownloadEntry> mWaitingQueue = new LinkedBlockingQueue<>();
    //异常：1.net error 2.no sd 3.no memory
    public static final int NOTIFY_DOWNLOADING = 0x101;
    public static final int NOTIFY_COMPLETED = 0x102;
    public static final int NOTIFY_UPDATING = 0x103;
    public static final int NOTIFY_PAUSED_OR_CANCELED = 0x104;
    public static final int NOTIFY_CONNECTING = 0x105;
    public static final int NOTIFY_ERROR = 0x106;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case NOTIFY_PAUSED_OR_CANCELED:
                case NOTIFY_COMPLETED:
                case NOTIFY_ERROR:
                    checkNext((DownloadEntry) msg.obj);
                    break;
            }


            mDataChanger.postStatus((DownloadEntry) msg.obj);
        }
    };

    private void checkNext(DownloadEntry entry) {

        mDownloadingTasks.remove(entry.getId());
        if (mWaitingQueue.size() > 0) {
            DownloadEntry next = mWaitingQueue.poll();
            if (next != null) {
                startDownload(next);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mExecutors = Executors.newCachedThreadPool();
        mDataChanger = DataChanger.getInstance(getApplicationContext());
        mDatabase = DownloadDatabase.getInstance(getApplicationContext());
        initializeDownload();

        Trace.d("DownloaderService onCreate ");

    }

    private void initializeDownload() {
        new Handler().postDelayed(() -> new Thread(() -> {
            List<DownloadEntry> entries = mDatabase.downloadEntryDao().getAllDownloads();

            for (int i = 0; i < entries.size(); i++) {
                DownloadEntry entry = entries.get(i);

                if (entry.getStatus() == DownloadStatus.DOWNLOADING || entry.getStatus() == DownloadStatus.WAITING) {
                    if (DownloadConfig.getRecoverDownloadWhenStart()) {
                        if (entry.isSupportRange()) {
                            entry.setStatus(DownloadStatus.PAUSED);
                        } else {
                            entry.setStatus(DownloadStatus.IDLE);
                            entry.reset();
                        }
                        addDownload(entry);
                    } else {
                        if (entry.isSupportRange()) {
                            entry.setStatus(DownloadStatus.PAUSED);
                        } else {
                            entry.setStatus(DownloadStatus.IDLE);
                            entry.reset();
                        }
                        mDatabase.downloadEntryDao().insertOrUpdate(entry);
                    }
                }
                mDataChanger.addToOperatedEntryMap(entry.getId(),entry);
            }
        }).start(),2000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {

            DownloadEntry entry = intent.getParcelableExtra(Constants.KEY_DOWNLOAD_ENTRY);

            if (entry != null) {
                if (mDataChanger.constainsDownloadEntry(entry.getId())) {
                    entry = mDataChanger.queryDownloadEntryById(entry.getId());
                }
            }
            int action = intent.getIntExtra(Constants.KEY_DOWNLOAD_ACTION, -1);
            doAction(entry, action);
        }


        return super.onStartCommand(intent, flags, startId);
    }

    private void doAction(DownloadEntry entry, int action) {
        //check action, do related action
        switch (action) {
            case Constants.KEY_DOWNLOAD_ACTION_ADD:
                addDownload(entry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_PAUSE:
                pauseDownload(entry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_RESUME:
                resumeDownload(entry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_CANCEL:
                cancelDownload(entry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_PAUSE_ALL:
                pauseAll();
                break;
            case Constants.KEY_DOWNLOAD_ACTION_RECOVER_ALL:
                recoverAll();
                break;
        }
    }

    private void recoverAll() {
        ArrayList<DownloadEntry> mRecoverableEntries = DataChanger.getInstance(getApplicationContext()).queryAllRecoverableEntries();
        if (mRecoverableEntries != null && mRecoverableEntries.size() > 0) {
            for (DownloadEntry downloadEntry : mRecoverableEntries) {
                addDownload(downloadEntry);
            }
        }

    }

    private void pauseAll() {
        while (mWaitingQueue.iterator().hasNext()) {
            DownloadEntry next = mWaitingQueue.poll();
            if (next != null) {

                next.setStatus(DownloadStatus.PAUSED);
                DataChanger.getInstance(getApplicationContext()).postStatus(next);
            }
        }

        for (HashMap.Entry<String, DownloaderTask> entry : mDownloadingTasks.entrySet()) {
            DownloaderTask value = entry.getValue();
            value.pause();
        }

        mDownloadingTasks.clear();

    }

    private void addDownload(DownloadEntry entry) {
        if (mDownloadingTasks.size() >= DownloadConfig.getMaxDownloadTasks()) {
            mWaitingQueue.offer(entry);
            entry.setStatus(DownloadStatus.WAITING);

            DataChanger.getInstance(getApplicationContext()).postStatus(entry);
        } else {
            startDownload(entry);
        }
    }

    private void cancelDownload(DownloadEntry entry) {
        DownloaderTask task = mDownloadingTasks.remove(entry.getId());

        if (task != null) {
            task.cancel();
        } else {
            mWaitingQueue.remove(entry);
            entry.setStatus(DownloadStatus.CANCELLED);
            DataChanger.getInstance(getApplicationContext()).postStatus(entry);
        }
    }

    private void resumeDownload(DownloadEntry entry) {
        addDownload(entry);
    }

    private void pauseDownload(DownloadEntry entry) {
        DownloaderTask task = mDownloadingTasks.remove(entry.getId());
        if (task != null) {
            task.pause();
        } else {
            mWaitingQueue.remove(entry);
            entry.setStatus(DownloadStatus.PAUSED);
            DataChanger.getInstance(getApplicationContext()).postStatus(entry);
        }
    }

    private void startDownload(DownloadEntry entry) {
        //FIXME 切换网络3g 没有内存 ，没有sd卡
        DownloaderTask task = new DownloaderTask(entry, mHandler,mExecutors);
        task.start();
        mDownloadingTasks.put(entry.getId(), task);
    }
}
