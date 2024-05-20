package com.xaye.downloader;

import android.content.Context;
import android.content.Intent;

import com.xaye.downloader.core.DownloaderService;
import com.xaye.downloader.entities.DownloadEntry;
import com.xaye.downloader.notify.DataChanger;
import com.xaye.downloader.notify.DataWatcher;
import com.xaye.downloader.utilities.Constants;

import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @FileName:com.xaye.downloader.DownloaderManager.kt Author xaye
 * @date: 2024-04-05 08:44
 * Created by 11623 on 2024/4/5
 */
public class DownloaderManager {
    private static DownloaderManager instance;
    private final Context context;

    private DownloaderManager(Context context){
        this.context = context;
        context.startService(new Intent(context, DownloaderService.class));
    }

    public static DownloaderManager getInstance(Context context){
        if(instance == null){
            instance = new DownloaderManager(context);
        }
        return instance;
    }

    public void add(DownloadEntry entry){
        Intent intent = new Intent(context,DownloaderService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY,entry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION,Constants.KEY_DOWNLOAD_ACTION_ADD);
        context.startService(intent);
    }
    public void pause(DownloadEntry entry){
        Intent intent = new Intent(context,DownloaderService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY,entry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION,Constants.KEY_DOWNLOAD_ACTION_PAUSE);
        context.startService(intent);
    }

    public void pauseAll(){
        Intent intent = new Intent(context,DownloaderService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION,Constants.KEY_DOWNLOAD_ACTION_PAUSE_ALL);
        context.startService(intent);
    }

    public void recoverAll(){
        Intent intent = new Intent(context,DownloaderService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION,Constants.KEY_DOWNLOAD_ACTION_RECOVER_ALL);
        context.startService(intent);
    }

    public void resume(DownloadEntry entry){
        Intent intent = new Intent(context,DownloaderService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY,entry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION,Constants.KEY_DOWNLOAD_ACTION_RESUME);
        context.startService(intent);
    }
    public void cancel(DownloadEntry entry){
        Intent intent = new Intent(context,DownloaderService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY,entry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION,Constants.KEY_DOWNLOAD_ACTION_CANCEL);
        context.startService(intent);
    }

    public void addObserver(DataWatcher dataWatcher) {
        DataChanger.getInstance(context).addObserver(dataWatcher);
    }

    public void deleteObserver(DataWatcher dataWatcher) {
        DataChanger.getInstance(context).deleteObserver(dataWatcher);
    }

    public DownloadEntry queryDownloadEntry(@Nullable String id) {
        return DataChanger.getInstance(context).queryDownloadEntryById(id);
    }

    public void deleteDownloadEntry(boolean forceDelete,String id) {
        DataChanger.getInstance(context).deleteDownloadEntry(id);
        // TODO: 2024/4/5 删除文件
        if (forceDelete) {
            File file = DownloadConfig.getDownloadFile(id);
            if(file.exists()){
                file.delete();
            }
        }
    }
}
