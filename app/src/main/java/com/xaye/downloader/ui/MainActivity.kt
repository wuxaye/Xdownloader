package com.xaye.downloader.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.xaye.downloader.DownloaderManager
import com.xaye.downloader.databinding.ActivityMainBinding
import com.xaye.downloader.entities.DownloadEntry
import com.xaye.downloader.entities.DownloadStatus
import com.xaye.downloader.listener.DownLoadListener
import com.xaye.downloader.notification.DownloadNotificationManager
import com.xaye.downloader.utils.Trace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


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
        binding.btnDownload.setOnClickListener {

            entry = DownloaderManager.download(tag = "WifiKey",
                url = "https://down11.zol.com.cn/liaotiao/WifiKey5.0.0w.apk",
                reDownload = true,
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
                        binding.tvProgress.text = "$progress%"
                        binding.progressBar.progress = progress

                    }

                    override fun onDownLoadPrepare(key: String) {
                       Trace.d("onDownLoadPrepare key: $key")
                        binding.tvProgress.text = "Prepare"
                    }

                    override fun onDownLoadError(
                        key: String,
                        errorMsg: String?,
                        throwable: Throwable?
                    ) {
                        Trace.e("onDownLoadError key: $key, throwable: $throwable")
                        binding.tvProgress.text = "$errorMsg"
                    }

                    @SuppressLint("SetTextI18n")
                    override fun onDownLoadSuccess(key: String, path: String, size: Long) {
                        Trace.d("onDownLoadSuccess key: $key, path: $path, size: $size")
                        binding.tvProgress.text = 100.toString() + "%"
                        binding.progressBar.progress = 100

                    }

                    override fun onDownLoadPause(key: String) {
                        Trace.d("onDownLoadPause key: $key")
                        binding.tvProgress.text = "Pause"
                    }

                    override fun onDownLoadCancel(key: String) {
                        Trace.d("onDownLoadCancel key: $key")
                        binding.tvProgress.text = "Cancel"
                        binding.progressBar.progress = 0
                    }

                })

        }

        binding.btnPause.setOnClickListener {
            if (entry?.status == DownloadStatus.DOWNLOADING) {
                entry?.let { DownloaderManager.pause(it) }
            } else if (entry?.status == DownloadStatus.PAUSED){
                entry?.let {  DownloaderManager.resume(it) }
            }
        }

        binding.btnCancel.setOnClickListener {
            entry?.let { DownloaderManager.cancel(it) }
        }

    }

    override fun onResume() {
        super.onResume()
       DownloaderManager.getObserverLiveData().observe(this) { data ->
           entry = data
           //Trace.e(data.toString())
       }

        CoroutineScope(Dispatchers.IO).launch {
            DownloaderManager.getObserverFlow().collectLatest { entry ->
                Trace.e("Flow 1-> $entry")
            }
        }
    }

}