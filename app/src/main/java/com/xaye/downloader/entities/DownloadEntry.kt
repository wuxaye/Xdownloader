package com.xaye.downloader.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.xaye.downloader.DownloadConfig.getDownloadFile
import com.xaye.downloader.db.Converters
import kotlinx.parcelize.Parcelize

/**
 * @FileName:com.xaye.downloader.DownloadEntry.kt
 * Author xaye
 * @date: 2024-04-27 16:31
 * Created by 11623 on 2024/4/27
 */
@Parcelize
@Entity(tableName = "downloads")
@TypeConverters(Converters::class)
data class DownloadEntry(
    @PrimaryKey(autoGenerate = true)
    var pid: Long? = null,

    var id: String,
    var name: String,
    var url: String,
    var status: DownloadStatus = DownloadStatus.IDLE,
    var currentLength: Int = 0,
    var totalLength: Int = 0,
    var isSupportRange: Boolean = false, // 是否支持断点续传
    var ranges : HashMap<Int,Int> = HashMap(),
    var percent: Int = 0
): Parcelable {
    //constructor():this(null,"","","", DownloadStatus.IDLE,0,0,false, HashMap(),0)


    override fun toString(): String {
        return "DownloadEntry :$url is $status with $currentLength/$totalLength"
    }

    fun reset() {
        currentLength = 0
        ranges.clear()
        percent = 0

        val file = getDownloadFile(url)
        if (file.exists()) {
            file.delete()
        }
    }
}

enum class DownloadStatus {
    IDLE,
    CONNECTING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED,
    WAITING,
    ERROR
}

