package com.xaye.downloader.ui

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.xaye.downloader.DownloaderManager
import com.xaye.downloader.databinding.ActivityMainBinding
import com.xaye.downloader.entities.DownloadEntry
import com.xaye.downloader.entities.DownloadStatus
import com.xaye.downloader.listener.DownLoadListener
import com.xaye.downloader.utilities.Trace


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mDownloaderManager: DownloaderManager

    var entry : DownloadEntry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        XXPermissions.with(this)
            .permission(Permission.WRITE_EXTERNAL_STORAGE)
            .permission(Permission.READ_EXTERNAL_STORAGE)
            .request { permissions, all ->
                if (all) {
                    Trace.e("获取权限成功")
                } else {
                    Trace.e("获取权限失败")
                }
            }

        DownloaderManager.init(this)
        binding.btnDownloader.setOnClickListener {
            DownloaderManager.download(this, tag = "WifiKey",
                url = "https://down11.zol.com.cn/liaotiao/WifiKey5.0.0w.apk",
                savePath = Environment.getExternalStorageDirectory().absolutePath + "/CustomDownload/",
                saveName = "WifiKey5.0.0w.apk",
                listener = object :
                    DownLoadListener {
                    override fun onUpdate(
                        key: String,
                        progress: Int,
                        read: Long,
                        count: Long,
                        done: Boolean
                    ) {
                        Trace.d("onUpdate key: $key, progress: $progress, read: $read, count: $count, done: $done")
                    }

                    override fun onDownLoadPrepare(key: String) {
                       Trace.d("onDownLoadPrepare key: $key")
                    }

                    override fun onDownLoadError(key: String, throwable: Throwable) {
                        Trace.e("onDownLoadError key: $key, throwable: $throwable")
                    }

                    override fun onDownLoadSuccess(key: String, path: String, size: Long) {
                        Trace.d("onDownLoadSuccess key: $key, path: $path, size: $size")
                    }

                    override fun onDownLoadPause(key: String) {
                        Trace.d("onDownLoadPause key: $key")
                    }

                })

        }

        binding.btnPause.setOnClickListener {
            if (entry?.status == DownloadStatus.DOWNLOADING) {
                //DownloaderManager.pause(entry)
            } else if (entry?.status == DownloadStatus.PAUSED){
               // DownloaderManager.resume(entry)
            }
        }

        binding.btnCancel.setOnClickListener {
           // DownloaderManager.cancel(entry)
        }

    }

    override fun onResume() {
        super.onResume()
       DownloaderManager.getObserver().observe(this) { data ->
           entry = data
           Trace.e(data.toString())
       }
    }

}