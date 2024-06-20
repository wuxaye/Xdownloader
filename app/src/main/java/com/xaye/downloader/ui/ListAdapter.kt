package com.xaye.downloader.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.xaye.downloader.DownloaderManager
import com.xaye.downloader.databinding.MissionItemLinearBinding
import com.xaye.downloader.entities.DownloadEntry
import com.xaye.downloader.entities.DownloadStatus
import com.xaye.downloader.utils.TextUtil

/**
 * @FileName:com.xaye.downloader.ui.ListAdapter.kt
 * Author xaye
 * @date: 2024-04-05 13:39
 * Created by 11623 on 2024/4/5
 */
class ListAdapter(datas: MutableList<DownloadEntry>) :
    BaseQuickAdapter<DownloadEntry, ListAdapter.VH>(datas) {

    // 自定义ViewHolder类
    class VH(
        parent: ViewGroup,
        val binding: MissionItemLinearBinding = MissionItemLinearBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ),
    ) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int, item: DownloadEntry?) {
        if (item != null) {
            holder.binding.apply {
                itemName.text = item.name
                itemSize.text = TextUtil.getTotalLengthText(item.totalLength.toLong())
                itemStatus.text = when (item.status) {
                    DownloadStatus.IDLE -> "空闲"
                    DownloadStatus.DOWNLOADING -> String.format("%.0f%%", item.percent.toFloat())
                    DownloadStatus.PAUSED -> "暂停中"
                    DownloadStatus.COMPLETED -> "下载完成"
                    DownloadStatus.FAILED -> "下载失败"
                    DownloadStatus.CANCELLED -> "已取消"
                    DownloadStatus.WAITING -> "等待中"
                    DownloadStatus.ERROR -> "下载错误：${item.exception?.errorMsg}"
                    DownloadStatus.CONNECTING -> "连接中"
                }

                when(item.status) {
                    DownloadStatus.IDLE -> {

                    }
                    DownloadStatus.DOWNLOADING -> {
                        itemMore.resume()
                        itemMore.visibility = View.VISIBLE
                        itemMore.progress = item.percent.toFloat()
                        itemSize.text = "${TextUtil.getSpeedText(item.speed)} ${
                            TextUtil.getTimeLeftText(
                                item.speed,
                                item.percent,
                                item.totalLength.toLong()
                            )
                        }"
                    }
                    DownloadStatus.PAUSED -> {
                        itemMore.pause()
                    }
                    DownloadStatus.COMPLETED -> {
                        itemMore.visibility = View.GONE
                        itemMore.pause()

                        if (item.currentLength == 0) {
                            itemSize.visibility = View.GONE
                        }
                    }
                    DownloadStatus.FAILED -> {
                        itemMore.visibility = View.VISIBLE
                        itemMore.pause()
                    }
                    DownloadStatus.CANCELLED -> {
                        itemMore.visibility = View.VISIBLE
                        itemMore.pause()
                    }
                    DownloadStatus.WAITING -> {
                        itemMore.visibility = View.VISIBLE
                        itemMore.pause()
                    }
                    DownloadStatus.ERROR -> {
                        itemSize.text = ""
                        itemMore.visibility = View.VISIBLE
                        itemMore.pause()
                    }
                    DownloadStatus.CONNECTING -> {
                        itemMore.visibility = View.VISIBLE
                        itemMore.pause()
                    }
                }

                itemMore.setOnClickListener {
                    when (item.status) {
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
        }

    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int) = VH(parent)

}