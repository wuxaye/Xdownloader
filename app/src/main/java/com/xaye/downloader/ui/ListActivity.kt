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

    private lateinit var mDownloaderManager: DownloaderManager

    private val watcher = object : DataWatcher() {
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

        val entry1 = DownloadEntry(id = "1", name =  "西瓜视频", url = "http://qn.yingyonghui.com/apk/7092466/992d3dcf9b204b0e6c0bd17c349c3e5c?sign=cc8e454a738ec485d94fb207eda324df&t=6644c2d0&attname=992d3dcf9b204b0e6c0bd17c349c3e5c.apk")
        val entry2 = DownloadEntry(id = "2", name =  "番茄畅听", url = "http://qn.yingyonghui.com/apk/7091064/2f6fa6102a261dd83226b4e4c69de2c6?sign=f25065fd4ac479b920e1e11cda8b7c1f&t=6644c469&attname=2f6fa6102a261dd83226b4e4c69de2c6.apk")
        val entry3 = DownloadEntry(id = "3", name =  "脉脉", url = "http://qn.yingyonghui.com/apk/7085719/a5b22937feb201ce11b7dd0742457788?sign=bc98c8f551a6367aa8e5a99b47ec4c4b&t=6644c496&attname=a5b22937feb201ce11b7dd0742457788.apk")
        val entry4 = DownloadEntry(id = "4", name =  "今日头条", url = "http://qn.yingyonghui.com/apk/7092465/ce257e5f0252ade885314fe9ec44dc77?sign=037a372571fb81f0420ae2dbf860c219&t=6644c4b2&attname=ce257e5f0252ade885314fe9ec44dc77.apk")
        val entry5 = DownloadEntry(id = "5", name =  "快手", url = "http://qn.yingyonghui.com/apk/7088289/adb0e4241e54de4965d09454f8f4a21a?sign=a3db10453405e6c40f7a73975e8d81f3&t=6644c4e2&attname=adb0e4241e54de4965d09454f8f4a21a.apk")
        val entry6 = DownloadEntry(id = "6", name =  "还呗", url = "http://qn.yingyonghui.com/apk/7089099/7af5ef9402216dba6f4710a437332fea?sign=8be71f23318892f79ba6ba54d7281f9f&t=6644c4fe&attname=7af5ef9402216dba6f4710a437332fea.apk")


        mDownloadEntries.add(entry1)
        mDownloadEntries.add(entry2)
        mDownloadEntries.add(entry3)
        mDownloadEntries.add(entry4)
        mDownloadEntries.add(entry5)
        mDownloadEntries.add(entry6)


        mDownloaderManager = DownloaderManager.getInstance(this)


        var entry: DownloadEntry? = null
        var realEntry: DownloadEntry? = null
        mDownloadEntries.indices.forEach {
            entry = mDownloadEntries[it]
            realEntry = mDownloaderManager.queryDownloadEntry(entry?.id)
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
                mDownloaderManager.pauseAll()
                binding.btnPauseAll.text = "恢复全部"
            } else {
                mDownloaderManager.recoverAll()
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
        mDownloaderManager.addObserver(watcher)
    }

    override fun onPause() {
        super.onPause()
        mDownloaderManager.deleteObserver(watcher)
    }
}