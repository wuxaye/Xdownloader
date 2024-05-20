package com.xaye.downloader.ui

import android.content.Context
import android.view.ViewGroup
import android.widget.Button
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.xaye.downloader.entities.DownloadEntry
import com.xaye.downloader.DownloaderManager
import com.xaye.downloader.R
import com.xaye.downloader.utilities.Trace
import com.xaye.downloader.entities.DownloadStatus

/**
 * @FileName:com.xaye.downloader.ui.ListAdapter.kt
 * Author xaye
 * @date: 2024-04-05 13:39
 * Created by 11623 on 2024/4/5
 */
class ListAdapter(datas: MutableList<DownloadEntry>) : BaseQuickAdapter<DownloadEntry, QuickViewHolder>(datas) {
    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: DownloadEntry?) {

        holder.setText(R.id.tv_name,"${item?.name} is ${item?.status} ${item?.currentLength}/${item?.totalLength}")

        holder.getView<Button>(R.id.btn_downloader).setOnClickListener {


            Trace.d(" list btn_downloader item?.status = ${item?.status}")
            if (item?.status == DownloadStatus.IDLE) {
                DownloaderManager.getInstance(context).add(item)
            } else if (item?.status == DownloadStatus.DOWNLOADING ||
                item?.status == DownloadStatus.WAITING){
                DownloaderManager.getInstance(context).pause(item)
            } else if (item?.status == DownloadStatus.PAUSED){
                DownloaderManager.getInstance(context).resume(item)
            }
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.list_item,parent)
    }
}