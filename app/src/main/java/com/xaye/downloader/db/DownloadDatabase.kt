package com.xaye.downloader.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import com.xaye.downloader.entities.DownloadEntry
import java.io.File

/**
 * @FileName:com.xaye.downloader.db.DownloadDatabase.kt
 * Author xaye
 * @date: 2024-04-27 16:54
 * Created by 11623 on 2024/4/27
 */
@Database(entities = [DownloadEntry::class], version = 1, exportSchema = false)
abstract class DownloadDatabase : RoomDatabase(){
    abstract fun downloadEntryDao(): DownloadEntryDao

    companion object {
        @Volatile
        private var downloadDatabase: DownloadDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): DownloadDatabase {
            if (downloadDatabase == null) {
                synchronized(DownloadDatabase::class.java) {
                    if (downloadDatabase == null) {
                        downloadDatabase = databaseBuilder(
                            context, DownloadDatabase::class.java,
                            context.getExternalFilesDir("database")
                                .toString() + File.separator + "downloadDB.db"
                        ).build()
                    }
                }
            }
            return downloadDatabase!!
        }
    }

}