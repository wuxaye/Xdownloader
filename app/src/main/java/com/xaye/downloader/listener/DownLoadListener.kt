package com.xaye.downloader.listener

/**
 * Author xaye
 * @date: 2024/5/31
 */
interface DownLoadListener {

    //更新进度
    fun onUpdate(key: String, progress: Int, read: Long, count: Long, done: Boolean)

    //等待下载
    fun onDownLoadPrepare(key: String)

    //下载失败
    fun onDownLoadError(key: String, errorMsg: String, throwable: Throwable)

    //下载成功
    fun onDownLoadSuccess(key: String, path: String, size: Long)

    //下载暂停
    fun onDownLoadPause(key: String)

    //下载取消
    fun onDownLoadCancel(key: String)
}