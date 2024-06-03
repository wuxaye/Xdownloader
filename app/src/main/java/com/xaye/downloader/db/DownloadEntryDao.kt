package com.xaye.downloader.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.xaye.downloader.entities.DownloadEntry
import com.xaye.downloader.entities.DownloadStatus

/**
 * @FileName:com.xaye.downloader.db.DownloadEntryDao.kt
 * Author xaye
 * @date: 2024-04-27 16:48
 * Created by 11623 on 2024/4/27
 */
@Dao
interface DownloadEntryDao {
    @Query("SELECT * FROM downloads")
    suspend fun getAllDownloads(): List<DownloadEntry>

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getDownloadById(id: String): DownloadEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(downloadEntry: DownloadEntry)

    @Delete
    suspend fun deleteDownload(downloadEntry: DownloadEntry): Int

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownloadById(id: String): Int

    @Query("UPDATE downloads SET status = :status WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, status: DownloadStatus)

    @Query("UPDATE downloads SET currentLength = :currentLength WHERE id = :id")
    suspend fun updateDownloadCurrentLength(id: String, currentLength: Int)

    @Query("UPDATE downloads SET totalLength = :totalLength WHERE id = :id")
    suspend fun updateDownloadTotalLength(id: String, totalLength: Int)
}