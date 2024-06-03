package com.xaye.downloader.entities

/**
 * Author xaye
 * @date: 2024/6/1
 */
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