package com.xaye.downloader.utils

/**
 * Author xaye
 * @date: 2024-05-26 19:06
 */
object Constants {
    const val KEY_DOWNLOAD_ENTRY = "key_download_entry"

    const val KEY_DOWNLOAD_ACTION = "key_download_action"

    const val KEY_DOWNLOAD_ACTION_ADD = 0x01

    const val KEY_DOWNLOAD_ACTION_PAUSE = 0x02

    const val KEY_DOWNLOAD_ACTION_RESUME = 0x03

    const val KEY_DOWNLOAD_ACTION_CANCEL = 0x04

    const val KEY_DOWNLOAD_ACTION_PAUSE_ALL = 0x05

    const val KEY_DOWNLOAD_ACTION_RECOVER_ALL = 0x06

    const val CONNECT_TIMEOUT = 3000

    const val READ_TIMEOUT = 5000

    const val MAX_VALUE_PROGRESS = 100

    const val KEY_REQUEST_ID = "key_request_id"

    const val KEY_FILE_NAME = "key_fileName"

    const val KEY_LENGTH = "key_length"

    const val DEFAULT_VALUE_LENGTH = 0L
}