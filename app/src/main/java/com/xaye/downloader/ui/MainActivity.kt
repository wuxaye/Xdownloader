package com.xaye.downloader.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.xaye.downloader.notify.DataWatcher
import com.xaye.downloader.entities.DownloadEntry
import com.xaye.downloader.DownloaderManager
import com.xaye.downloader.utilities.Trace
import com.xaye.downloader.databinding.ActivityMainBinding
import com.xaye.downloader.entities.DownloadStatus

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var mDownloaderManager: DownloaderManager

    var entry : DownloadEntry? = null

    private val watcher = object : DataWatcher() {
        override fun notifyUpdate(data: DownloadEntry) {
            entry = data
//            if (entry?.status == DownloadStatus.CANCELLED) {
//                entry = null
//            }

            Trace.e(data.toString())
        }

    }

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


        binding.btnDownloader.setOnClickListener {
//
//            if (entry == null) {
//                entry = DownloadEntry()
//                entry!!.name = "officeIPC_1.apk"
//                entry!!.url = "http://47.93.99.16:85/download/treadmill/dingkang/app/kuwo/V8.5.0.4/V8.5.0.4.apk"
//                entry!!.id = "1"
//            }
//
//            mDownloaderManager.add(entry)

        }

        binding.btnPause.setOnClickListener {
            if (entry?.status == DownloadStatus.DOWNLOADING) {
                mDownloaderManager.pause(entry)
            } else if (entry?.status == DownloadStatus.PAUSED){
                mDownloaderManager.resume(entry)
            }
        }

        binding.btnCancel.setOnClickListener {
            mDownloaderManager.cancel(entry)
        }

        mDownloaderManager = DownloaderManager.getInstance(this)
    }

    override fun onResume() {
        super.onResume()
        mDownloaderManager.addObserver(watcher)
    }

    override fun onPause() {
        super.onPause()
        mDownloaderManager.deleteObserver(watcher)
    }
}