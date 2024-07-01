package com.xaye.downloader

import android.os.Environment
import com.xaye.downloader.utils.FileUtils
import java.io.File

/**
 * @FileName:com.xaye.downloader.DownloadConfig.kt
 * Author xaye
 * @date: 2024-05-12 20:50
 * Created by 11623 on 2024/5/12
 */
object DownloadConfig {
    //最大下载任务数
    private var max_download_tasks = 3

    //最大下载线程数
    private var max_download_threads = 3

    //下载目录
    private var downloadDir: String? = null

    //最小操作间隔即通知进度更新间隔
    private var min_operate_interval = 500L

    //在应用启动时是否自动恢复下载
    private var recoverDownloadWhenStart = false

    //最大重试次数
    private var max_retry_times = 3

    init {
        //安卓11 分区存储 /storage/emulated/0/Download 公用下载目录
        downloadDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
    }

    @JvmStatic
    fun getMaxDownloadTasks(): Int {
        return max_download_tasks
    }

    @JvmStatic
    fun getMaxDownloadThreads(): Int {
        return max_download_threads
    }

    @JvmStatic
    fun getDownloadDir(): String {
        return downloadDir!!
    }

    @JvmStatic
    fun getMinOperateInterval(): Long {
        return min_operate_interval
    }

    @JvmStatic
    fun getRecoverDownloadWhenStart(): Boolean {
        return recoverDownloadWhenStart
    }

    @JvmStatic
    fun getMaxRetryTimes(): Int {
        return max_retry_times
    }

    @JvmStatic
    fun setMaxDownloadTasks(max_download_tasks: Int) {
        this.max_download_tasks = max_download_tasks
    }

    @JvmStatic
    fun setMaxDownloadThreads(max_download_threads: Int) {
        this.max_download_threads = max_download_threads
    }

    @JvmStatic
    fun setDownloadDir(downloadDir: String) {
        this.downloadDir = downloadDir
    }

    @JvmStatic
    fun setMinOperateInterval(min_operate_interval: Long) {
        this.min_operate_interval = min_operate_interval
    }

    @JvmStatic
    fun setRecoverDownloadWhenStart(recoverDownloadWhenStart: Boolean) {
        this.recoverDownloadWhenStart = recoverDownloadWhenStart
    }

    @JvmStatic
    fun setMaxRetryTimes(max_retry_times: Int) {
        this.max_retry_times = max_retry_times
    }

    /**
     * 默认文件保存路径、文件名
     */
    @JvmStatic
    fun getDefaultDownloadFile(url: String): File {
        return File(downloadDir, FileUtils.getMd5FileName(url))
    }
}