# downloader

## 介绍

多任务，多线程，断点续传下载框架，可供学习及练习使用

此框架将会持续更新，力求完美，欢迎star

<p align="center">
  <img width="500" height="auto" src="https://github.com/wuxaye/Xdownloader/blob/master/assets/Xdownload_logo.png" >
</p>

## 软件架构

Kotlin + Room + Flow + 协程


## 功能

- 多任务下载，设置最大任务数

- 下载数据同步更新Room数据库保存，实现断点续传功能

- 下载任务队列，超过最大下载任务数，进入等待状态，下载任务完成后，等待任务自动唤醒

- 自动恢复上次下载任务

- 下载异常自动重试，设置最大重试次数，重试时间间隔

- 可选 `LiveData` 或 `Flow` 监听下载状态

- 状态栏通知栏下载进度


## 截图


<p align="center">
 <img width="300" height="auto" src="https://github.com/wuxaye/Xdownloader/blob/master/assets/single.png" >  <img width="300" height="auto" src="https://github.com/wuxaye/Xdownloader/blob/master/assets/multi.png" >
</p>

<p align="center">
  <img width="300" height="auto" src="https://github.com/wuxaye/Xdownloader/blob/master/assets/notify1.png" >   <img width="300" height="auto" src="https://github.com/wuxaye/Xdownloader/blob/master/assets/notify2.png" >
</p>


## 问题

发现下载出现网络异常后，再进行重新下载，下载完成的文件状态是下载中！OPEN

## 计划

继续优化此框架。

增加文件下载完成可以打开文件功能。

增加状态栏通知下载进度功能。



