package com.xaye.downloader.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.xaye.downloader.DownloadConfig.getDownloadFile
import com.xaye.downloader.db.Converters
import com.xaye.downloader.network.DownloadException
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

    var id: String,//下载任务的唯一ID
    var name: String,
    var url: String,
    var status: DownloadStatus = DownloadStatus.IDLE,
    var currentLength: Int = 0,
    var totalLength: Int = 0,
    var isSupportRange: Boolean = false, // 是否支持断点续传
    var ranges : HashMap<Int,Int> = HashMap(),//记录每个线程下载的进度
    var percent: Int = 0,
    var speed: Float = 0F, //下载速度 剩余时间
    var exception: DownloadException? = null
): Parcelable {

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

