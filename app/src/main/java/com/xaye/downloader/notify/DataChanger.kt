package com.xaye.downloader.notify

import android.content.Context
import com.xaye.downloader.db.DownloadDatabase
import com.xaye.downloader.entities.DownloadEntry
import com.xaye.downloader.entities.DownloadStatus
import java.util.Observable

/**
 * Author xaye
 * @date: 2024-05-26 18:22
 */
class DataChanger private constructor(private val context: Context) : Observable() {

    private val operatedEntries = LinkedHashMap<String, DownloadEntry>()

    companion object {
        private const val TAG = "DataChanger"
        @Volatile
        private var instance: DataChanger? = null

        fun getInstance(context: Context): DataChanger {
            return instance ?: synchronized(this) {
                instance ?: DataChanger(context.applicationContext).also { instance = it }
            }
        }
    }

    fun postStatus(entry: DownloadEntry) {
        operatedEntries[entry.id] = entry

        Thread {
            val downloadDao = DownloadDatabase.getInstance(context).downloadEntryDao()
            val existingDownload = downloadDao.getDownloadById(entry.id)

            existingDownload?.let {
                // 此时 entry 是 ListActivity new 的 pid 为 null 而 existingDownload 是数据库的 pid 不为 null 的情况
                entry.pid = it.pid  // 需要把 pid 赋值给 entry，有了主键数据库才会执行替换操作！
            }
            downloadDao.insertOrUpdate(entry)

            // 获取更新后的所有下载数据，用于调试
            val datas = downloadDao.getAllDownloads()
            // Log.d(TAG, "postStatus datas size = ${datas.size}, datas = $datas")
        }.start()

        setChanged()
        notifyObservers(entry)
    }

    fun queryAllRecoverableEntries(): ArrayList<DownloadEntry> {
        return ArrayList<DownloadEntry>().apply {
            operatedEntries.values.filterTo(this) { it.status == DownloadStatus.PAUSED }
        }
    }

    fun queryDownloadEntryById(id: String): DownloadEntry? {
        return operatedEntries[id]
    }

    fun addToOperatedEntryMap(id: String, downloadEntry: DownloadEntry) {
        operatedEntries[id] = downloadEntry
    }

    fun containsDownloadEntry(id: String): Boolean {
        return operatedEntries.containsKey(id)
    }

    fun deleteDownloadEntry(id: String): Boolean {
        DownloadDatabase.getInstance(context).downloadEntryDao().deleteDownloadById(id)
        return operatedEntries.remove(id) != null
    }
}