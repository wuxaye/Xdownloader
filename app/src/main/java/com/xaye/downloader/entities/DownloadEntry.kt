package com.xaye.downloader.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.xaye.downloader.DownloadConfig
import com.xaye.downloader.DownloadConfig.getDefaultDownloadFile
import com.xaye.downloader.db.Converters
import com.xaye.downloader.listener.DownLoadListener
import com.xaye.downloader.network.DownloadException
import com.xaye.downloader.utils.FileUtils.getUniqueId
import kotlinx.parcelize.Parcelize
import java.io.File

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
    var id: Long? = null,//主键id
    var key: String,//下载任务的唯一key
    var fileName: String,
    var url: String,
    var status: DownloadStatus = DownloadStatus.IDLE,
    var currentLength: Int = 0,
    var totalLength: Int = 0,
    var isSupportRange: Boolean = false, // 是否支持断点续传
    var ranges: HashMap<Int, Int> = HashMap(),//记录每个线程下载的进度
    var percent: Int = 0,
    var speed: Float = 0F, //下载速度 剩余时间
    var exception: DownloadException? = null, //下载异常情况
    var path: String = getDefaultDownloadFile(url).absolutePath, //保存到本地的目标文件路径
    var reDownload: Boolean = false,//是否强制重新下载
    var uniqueId: Int = getUniqueId(url, path, fileName),
) : Parcelable {

    override fun toString(): String {
        return "DownloadEntry :$url is $status with $currentLength/$totalLength"
    }

    fun reset() {
        currentLength = 0
        ranges.clear()
        percent = 0

        val file = getDefaultDownloadFile(url)
        if (file.exists()) {
            file.delete()
        }
    }

    fun notifyListener(listener: DownLoadListener) {
        when (this.status) {
            DownloadStatus.IDLE -> {
                // No specific callback required for IDLE state
            }

            DownloadStatus.CONNECTING, DownloadStatus.WAITING -> {
                listener.onDownLoadPrepare(this.key)
            }

            DownloadStatus.DOWNLOADING -> {
                listener.onUpdate(
                    this.key,
                    this.percent,
                    this.currentLength.toLong(),
                    this.totalLength.toLong(),
                    this.currentLength == this.totalLength
                )
            }

            DownloadStatus.PAUSED -> {
                listener.onDownLoadPause(this.key)
            }

            DownloadStatus.COMPLETED -> {
                listener.onDownLoadSuccess(
                    this.key,
                    path,
                    this.currentLength.toLong()
                )
            }

            DownloadStatus.FAILED, DownloadStatus.ERROR -> {
                listener.onDownLoadError(this.key,this.exception?.errorMsg,this.exception?.throwable)
            }

            DownloadStatus.CANCELLED -> {
                listener.onDownLoadCancel(this.key)
            }
        }
    }

}

