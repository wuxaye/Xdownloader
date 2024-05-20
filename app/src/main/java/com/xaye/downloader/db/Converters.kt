package com.xaye.downloader.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xaye.downloader.entities.DownloadStatus

/**
 * @FileName:com.xaye.downloader.db.DownloadStatusConverter.kt
 * Author xaye
 * @date: 2024-04-27 16:47
 * Created by 11623 on 2024/4/27
 */
class Converters {
    @TypeConverter
    fun toDownloadStatus(value: String): DownloadStatus = enumValueOf(value)

    @TypeConverter
    fun fromDownloadStatus(status: DownloadStatus): String = status.name


    @TypeConverter
    fun fromHashMap(value: HashMap<Int, Int>): String {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    fun toHashMap(value: String): HashMap<Int, Int> {
        val gson = Gson()
        val type = object : TypeToken<HashMap<Int, Int>>() {}.type
        return gson.fromJson(value, type)
    }
}