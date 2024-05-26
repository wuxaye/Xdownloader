package com.xaye.downloader.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.xaye.downloader.notify.DataWatcher
import com.xaye.downloader.entities.DownloadEntry
import com.xaye.downloader.DownloaderManager
import com.xaye.downloader.utilities.Trace
import com.xaye.downloader.databinding.ActivityListBinding

/**
 * stay4j - 10
 */
class ListActivity : AppCompatActivity() {

    lateinit var binding: ActivityListBinding
    lateinit var adapter: ListAdapter
    var mDownloadEntries = arrayListOf<DownloadEntry>()

    private val watcher = object : DataWatcher {
        override fun notifyUpdate(data: DownloadEntry) {
            val index = mDownloadEntries.indexOfFirst { it.id == data.id }
            if (index != -1) {
                mDownloadEntries[index] = data
                runOnUiThread{
                    adapter.notifyItemChanged(index)
                }

                Log.i("TAG"," watcher  data = ${data.id} 000 thread = "+Thread.currentThread().name)
            } else {
                mDownloadEntries.add(data)
                runOnUiThread {
                    adapter.notifyItemInserted(mDownloadEntries.size - 1)
                }

                Log.i("TAG"," watcher data = ${data.id} 111 thread = "+Thread.currentThread().name)
            }

            Trace.e(data.toString())
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val entry1 = DownloadEntry(id = "1", name =  "QQ2024", url = "https://ucdl.25pp.com/fs08/2024/05/22/5/110_716b067b070f1e7351a0ac6a05ced58d.apk")
        val entry2 = DownloadEntry(id = "2", name =  "酷狗音乐", url = "https://ucdl.25pp.com/fs08/2024/05/15/0/110_c2436d2f687fe8db484798cf2521677b.apk")
        val entry3 = DownloadEntry(id = "3", name =  "wifi万能钥匙", url = "https://ucdl.25pp.com/fs08/2024/05/25/5/110_f92406c39f6c650cacd67bb10ab8d9bd.apk")
        val entry4 = DownloadEntry(id = "4", name =  "微信", url = "https://ucdl.25pp.com/fs08/2024/04/25/5/106_62d3b4571e2ad4fcd022dfd3eee40665.apk")
        val entry5 = DownloadEntry(id = "5", name =  "抖音", url = "https://ucdl.25pp.com/fs08/2024/05/22/10/120_c6207531dcd8af119e769e90fa538ad7.apk")
        val entry6 = DownloadEntry(id = "6", name =  "快手", url = "https://ucdl.25pp.com/fs08/2024/05/23/7/110_bcf940331a5adbc13b40f7d21b0f2df3.apk")


        mDownloadEntries.add(entry1)
        mDownloadEntries.add(entry2)
        mDownloadEntries.add(entry3)
        mDownloadEntries.add(entry4)
        mDownloadEntries.add(entry5)
        mDownloadEntries.add(entry6)


       // mDownloaderManager = DownloaderManager.getInstance(this)
        DownloaderManager.init(applicationContext)


        var entry: DownloadEntry? = null
        var realEntry: DownloadEntry? = null
        mDownloadEntries.indices.forEach {
            entry = mDownloadEntries[it]
            realEntry = DownloaderManager.queryDownloadEntry(entry!!.id)
            if (realEntry != null) {
                mDownloadEntries.removeAt(it)
                mDownloadEntries.add(it, realEntry!!)
            }
        }

        adapter = ListAdapter(mDownloadEntries)
        binding.recycleView.adapter = adapter
        binding.recycleView.layoutManager = LinearLayoutManager(this)

        binding.btnPauseAll.setOnClickListener {
            if (binding.btnPauseAll.text == "暂停全部") {
                DownloaderManager.pauseAll()
                binding.btnPauseAll.text = "恢复全部"
            } else {
                DownloaderManager.recoverAll()
                binding.btnPauseAll.text = "暂停全部"
            }

        }

    }

    override fun onResume() {
        super.onResume()
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
        DownloaderManager.addObserver(watcher)
    }

    override fun onPause() {
        super.onPause()
        DownloaderManager.deleteObserver(watcher)
    }
}