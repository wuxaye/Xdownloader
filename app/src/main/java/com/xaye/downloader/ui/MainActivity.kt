package com.xaye.downloader.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.xaye.downloader.entities.DownloadEntry
import com.xaye.downloader.DownloaderManager
import com.xaye.downloader.utilities.Trace
import com.xaye.downloader.databinding.ActivityMainBinding
import com.xaye.downloader.entities.DownloadStatus

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


        binding.btnDownloader.setOnClickListener {
//
//            if (entry == null) {
//                entry = DownloadEntry()
//                entry!!.name = "officeIPC_1.apk"
//                entry!!.url = "http://47.93.99.16:85/download/treadmill/dingkang/app/kuwo/V8.5.0.4/V8.5.0.4.apk"
//                entry!!.id = "1"
//            }
//
//            DownloaderManager.add(entry)

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