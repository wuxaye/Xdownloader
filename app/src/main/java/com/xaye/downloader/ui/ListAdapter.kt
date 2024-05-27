package com.xaye.downloader.ui

import android.content.Context
import android.view.ViewGroup
import android.widget.Button
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.xaye.downloader.DownloaderManager
import com.xaye.downloader.R
import com.xaye.downloader.entities.DownloadEntry
import com.xaye.downloader.entities.DownloadStatus
import com.xaye.downloader.utilities.TextUtil
import com.xaye.downloader.utilities.Trace

/**
 * @FileName:com.xaye.downloader.ui.ListAdapter.kt
 * Author xaye
 * @date: 2024-04-05 13:39
 * Created by 11623 on 2024/4/5
 */
class ListAdapter(datas: MutableList<DownloadEntry>) :
    BaseQuickAdapter<DownloadEntry, QuickViewHolder>(datas) {
    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: DownloadEntry?) {

        holder.setText(
            R.id.tv_name,
            "${item?.name} is ${item?.status} ${TextUtil.getTotalLengthText(item!!.currentLength.toLong())}/${
                TextUtil.getTotalLengthText(item!!.totalLength.toLong())
            }  \n" +
                    " ${
                        if (item!!.totalLength != 0)
                            "${TextUtil.getSpeedText(item!!.speed)}  ${
                                TextUtil.getTimeLeftText(
                                    item!!.speed,
                                    ((item.currentLength.toLong() * 100) / item.totalLength.toLong()).toInt(),
                                    item.totalLength.toLong(),
                                )
                            }"
                        else ""
                    }"
        )

        if (item!!.totalLength != 0) {
            Trace.e(" ListAdapter  speed:${item!!.speed}  progressPercent:${((item.currentLength.toLong() * 100) / item.totalLength.toLong()).toInt()}  lengthInBytes:${item.totalLength.toLong()}")
        }

        holder.getView<Button>(R.id.btn_downloader).setOnClickListener {


            Trace.d(" list btn_downloader item?.status = ${item?.status}")
            when (item?.status) {
                DownloadStatus.IDLE -> {
                    DownloaderManager.add(item)
                }

                DownloadStatus.DOWNLOADING, DownloadStatus.WAITING -> {
                    DownloaderManager.pause(item)
                }

                DownloadStatus.PAUSED -> {
                    DownloaderManager.resume(item)
                }

                else -> {}
            }
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.list_item, parent)
    }
}