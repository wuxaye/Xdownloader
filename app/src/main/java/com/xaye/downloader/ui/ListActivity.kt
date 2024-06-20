package com.xaye.downloader.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.xaye.downloader.entities.DownloadEntry
import com.xaye.downloader.DownloaderManager
import com.xaye.downloader.utils.Trace
import com.xaye.downloader.databinding.ActivityListBinding

/**
 * stay4j - 10
 */
class ListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListBinding
    private lateinit var adapter: ListAdapter
    private var mDownloadEntries = arrayListOf<DownloadEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val entry1 = DownloadEntry(key = "1", name =  "中关村在线", url = "https://down11.zol.com.cn/liaotian/ZOL_Android-v10.04.02-full-20240429_encrypted_zol-m_14_align.apk")
        val entry2 = DownloadEntry(key = "2", name =  "微信", url = "https://down11.zol.com.cn/liaotiao/weixin8047g.apk")
        val entry3 = DownloadEntry(key = "3", name =  "手机QQ", url = "https://down11.zol.com.cn/liaotiao/Android_9.0.30_64g.apk")
        val entry4 = DownloadEntry(key = "4", name =  "WIFI万能钥匙", url = "https://down11.zol.com.cn/liaotiao/WifiKey5.0.0w.apk")
        val entry5 = DownloadEntry(key = "5", name =  "酷狗音乐", url = "https://down11.zol.com.cn/liaotiao/KugouPlayer12.1.8g.apk")
        val entry6 = DownloadEntry(key = "6", name =  "应用宝", url = "https://down11.zol.com.cn/liaotiao/yingyongbao8.6.5w.apk")

        mDownloadEntries.add(entry1)
        mDownloadEntries.add(entry2)
        mDownloadEntries.add(entry3)
        mDownloadEntries.add(entry4)
        mDownloadEntries.add(entry5)
        mDownloadEntries.add(entry6)

        DownloaderManager.init(applicationContext)

        var entry: DownloadEntry? = null
        var realEntry: DownloadEntry? = null
        mDownloadEntries.indices.forEach {
            entry = mDownloadEntries[it]
            realEntry = DownloaderManager.queryDownloadEntry(entry!!.key)
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
            .request { _, all ->
                if (all) {
                    Trace.d("获取权限成功")
                } else {
                    Trace.e("获取权限失败")
                }
            }

        DownloaderManager.getObserverLiveData().observe(this) { data ->
            val index = mDownloadEntries.indexOfFirst { it.key == data.key }
            if (index != -1) {
                mDownloadEntries[index] = data
                runOnUiThread{
                    adapter.notifyItemChanged(index)
                }
            } else {
                mDownloadEntries.add(data)
                runOnUiThread {
                    adapter.notifyItemInserted(mDownloadEntries.size - 1)
                }
            }
            Trace.e(data.toString())
        }
    }
}