package com.xaye.downloader.notify;

import com.xaye.downloader.entities.DownloadEntry;

import java.util.Observable;
import java.util.Observer;

/**
 * @FileName:com.xaye.downloader.DataWatcher.kt Author xaye
 * @date: 2024-04-05 08:47
 * Created by 11623 on 2024/4/5
 */
public abstract class DataWatcher implements Observer {
    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof DownloadEntry) {
            notifyUpdate((DownloadEntry)arg);
        }
    }

    public abstract void notifyUpdate(DownloadEntry arg);
}
