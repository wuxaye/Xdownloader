package com.xaye.downloader.notify

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.xaye.downloader.db.DownloadDatabase
import com.xaye.downloader.entities.DownloadEntry
import com.xaye.downloader.entities.DownloadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Observable

/**
 * Author xaye
 * @date: 2024-05-26 18:22
 */
class DataChanger private constructor(private val context: Context) {
    private val operatedEntries = LinkedHashMap<String, DownloadEntry>()
    private val _entriesLiveData = MutableLiveData<DownloadEntry>()
    val entriesLiveData: LiveData<DownloadEntry> get() = _entriesLiveData
    //SupervisorJob()，保证 databaseScope 的生命周期管理，并避免子协程失败影响其他协程
    private val databaseScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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
        synchronized(operatedEntries) {
            operatedEntries[entry.id] = entry
        }

        databaseScope.launch {
            val downloadDao = DownloadDatabase.getInstance(context).downloadEntryDao()
            val existingDownload = downloadDao.getDownloadById(entry.id)

            existingDownload?.let {
                entry.pid = it.pid  // 从数据库中同步PID(如果存在)。room insertOrUpdate功能要确保主键PID
            }
            downloadDao.insertOrUpdate(entry)

            // 用主线程上的最新条目更新LiveData
            withContext(Dispatchers.Main) {
                _entriesLiveData.value = entry
            }
        }
    }

    fun queryAllRecoverableEntries(): List<DownloadEntry> {
        return synchronized(operatedEntries) {
            operatedEntries.values.filter { it.status == DownloadStatus.PAUSED }
        }
    }

    fun queryDownloadEntryById(id: String): DownloadEntry? {
        return synchronized(operatedEntries) {
            operatedEntries[id]
        }
    }

    fun addToOperatedEntryMap(id: String, downloadEntry: DownloadEntry) {
        synchronized(operatedEntries) {
            operatedEntries[id] = downloadEntry
        }
    }

    fun containsDownloadEntry(id: String): Boolean {
        return synchronized(operatedEntries) {
            operatedEntries.containsKey(id)
        }
    }

    fun deleteDownloadEntry(id: String): Boolean {
        databaseScope.launch {
            DownloadDatabase.getInstance(context).downloadEntryDao().deleteDownloadById(id)
        }
        return synchronized(operatedEntries) {
            operatedEntries.remove(id) != null
        }
    }
}
