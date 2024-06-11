package com.xaye.downloader.notify

import android.content.Context
import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.xaye.downloader.db.DownloadDatabase
import com.xaye.downloader.entities.DownloadEntry
import com.xaye.downloader.entities.DownloadStatus
import com.xaye.downloader.utilities.Trace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Author xaye
 * @date: 2024-05-26 18:22
 */
class DataChanger private constructor(private val context: Context) {
    private val operatedEntries = ConcurrentHashMap<String, DownloadEntry>()
    private val _entriesLiveData = MutableLiveData<DownloadEntry>()
    val entriesLiveData: LiveData<DownloadEntry> get() = _entriesLiveData

    private val _downloadStatus = MutableSharedFlow<DownloadEntry>(replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val downloadStatus: SharedFlow<DownloadEntry> get() = _downloadStatus

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

        operatedEntries[entry.key] = entry

        databaseScope.launch {
            val downloadDao = DownloadDatabase.getInstance(context).downloadEntryDao()
            val existingDownload = withContext(Dispatchers.IO) {
                downloadDao.getDownloadById(entry.key)
            }

            existingDownload?.let {
                entry.id = it.id // 从数据库中同步PID(如果存在)
            }

            withContext(Dispatchers.IO) {
                downloadDao.insertOrUpdate(entry)
                Trace.d("postStatus insertOrUpdateentry : $entry")
            }

            if (entry.status == DownloadStatus.COMPLETED) {
                //下载完 删除数据库中的记录
                withContext(Dispatchers.IO) {
                    //由于外部传来的entry 不带pid,所以需要从数据库中查询
                    val realEntry = downloadDao.getDownloadById(entry.key)
                    val delete = realEntry?.let { downloadDao.deleteDownload(it) }

                    Trace.d("postStatus COMPLETED realEntry url : ${realEntry?.url} delete : $delete")
                }
            }

            // 用主线程上的最新条目更新LiveData
            withContext(Dispatchers.Main) {
                _entriesLiveData.value = entry //livedata

                _downloadStatus.emit(entry)//flow
            }
        }
    }

    fun queryAllRecoverableEntries(): List<DownloadEntry> {
        return operatedEntries.values.filter { it.status == DownloadStatus.PAUSED }
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

    fun deleteDownloadEntry(key: String): Boolean {
        databaseScope.launch {
            withContext(Dispatchers.IO) {
                DownloadDatabase.getInstance(context).downloadEntryDao().deleteDownloadByKey(key)
            }
        }
        return operatedEntries.remove(key) != null
    }
}
