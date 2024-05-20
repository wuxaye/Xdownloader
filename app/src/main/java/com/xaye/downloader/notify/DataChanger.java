package com.xaye.downloader.notify;


import android.content.Context;

import com.xaye.downloader.db.DownloadDatabase;
import com.xaye.downloader.entities.DownloadEntry;
import com.xaye.downloader.db.DownloadEntryDao;
import com.xaye.downloader.entities.DownloadStatus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Observable;

/**
 * @FileName:com.xaye.downloader.DataChanger.kt Author xaye
 * @date: 2024-04-05 08:47
 * Created by 11623 on 2024/4/5
 */
public class DataChanger extends Observable {

    private static final String TAG = "DataChanger";
    private static DataChanger instance;

    private final Context mContext;
    private LinkedHashMap<String, DownloadEntry> m0peratedEntries;

    public static DataChanger getInstance(Context context) {
        if (instance == null) {
            instance = new DataChanger(context);
        }
        return instance;
    }

    private DataChanger(Context context) {
        this.mContext = context;
        m0peratedEntries = new LinkedHashMap<>();
    }

    public void postStatus(DownloadEntry entry) {

        m0peratedEntries.put(entry.getId(), entry);

        new Thread(() -> {

            DownloadEntryDao downloadDao = DownloadDatabase.getInstance(mContext).downloadEntryDao();
            DownloadEntry existingDownload = downloadDao.getDownloadById(entry.getId());

            //此时 entry 是ListActivity new 的 pid为 null 而 existingDownload 是数据库的 pid不为 null 的情况

            if (existingDownload != null) {
                //需要把pid 赋值给entry， 有了主键 数据库才会执行替换操作！
                entry.setPid(existingDownload.getPid());
                downloadDao.insertOrUpdate(entry);
                //Trace.d(TAG + " postStatus insertOrUpdate ->> existingDownload");
            } else {
                downloadDao.insertOrUpdate(entry);
                //Trace.d(TAG + " postStatus insertOrUpdate ->> entry");
            }

            List<DownloadEntry> datas = DownloadDatabase.getInstance(mContext).downloadEntryDao().getAllDownloads();

           // Trace.d(TAG + " postStatus datas size = " + datas.size() + " , datas = " + Arrays.toString(datas.toArray()));
        }).start();

        setChanged();
        notifyObservers(entry);
    }

    public ArrayList<DownloadEntry> queryAllRecoverableEntries() {
        ArrayList<DownloadEntry> entries = null;
        for (DownloadEntry entry : m0peratedEntries.values()) {
            if (entry.getStatus() == DownloadStatus.PAUSED) {
                if (entries == null) {
                    entries = new ArrayList<>();
                }
                entries.add(entry);
            }
        }

        return entries;
    }

    public DownloadEntry queryDownloadEntryById(String id) {
        return m0peratedEntries.get(id);
    }

    public void addToOperatedEntryMap(String id, DownloadEntry downloadEntry) {
        m0peratedEntries.put(id, downloadEntry);
    }

    public boolean constainsDownloadEntry(String id) {
        return m0peratedEntries.containsKey(id);
    }

    public boolean deleteDownloadEntry(String id) {
        DownloadDatabase.getInstance(mContext).downloadEntryDao().deleteDownloadById(id);
        return m0peratedEntries.remove(id) != null;
    }
}
