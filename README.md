# XDownloader

## 项目简介

XDownloader 是一个功能完善的 Android 多任务、多线程、断点续传下载框架。采用现代 Android 技术栈构建，提供高效稳定的文件下载能力，适用于生产环境和学习实践。

<p align="center">
  <img width="500" height="auto" src="https://github.com/wuxaye/Xdownloader/blob/master/assets/Xdownload_logo.png" >
</p>

## 技术架构

| 技术组件 | 说明 |
|---------|------|
| **语言** | Kotlin |
| **数据库** | Room (持久化下载状态) |
| **异步处理** | Kotlin Coroutines + Flow |
| **架构模式** | Service 后台服务 + 线程池 |
| **最低版本** | Android 7.0 (API 24) |
| **目标版本** | Android 13 (API 33) |

## 核心功能

### 任务管理

- **多任务并发下载**: 可配置最大同时下载数量（默认 3 个）
- **任务队列机制**: 超过最大任务数时自动进入等待队列，任务完成后自动唤醒
- **批量操作**: 支持暂停/恢复所有下载任务

### 下载控制

- **多线程分片下载**: 每个任务可使用多线程并行下载（默认 3 个线程），大幅提升下载速度
- **断点续传**: 基于 Range 协议实现断点续传，网络中断后可从断点继续
- **自动重试**: 下载失败自动重试，可配置最大重试次数（默认 3 次）
- **智能重连**: 识别服务器是否支持 Range，不支持时采用单线程下载

### 状态管理

- **状态持久化**: 使用 Room 数据库实时保存下载进度和状态，应用重启自动恢复
- **启动恢复**: 可配置应用启动时自动恢复未完成的下载任务
- **状态同步**: 数据与 UI 实时同步，支持 LiveData 和 Flow 两种监听方式

### 下载状态

完整支持 9 种下载状态：
- `IDLE` - 空闲
- `CONNECTING` - 连接中
- `DOWNLOADING` - 下载中
- `PAUSED` - 已暂停
- `COMPLETED` - 下载完成
- `FAILED` - 下载失败
- `CANCELLED` - 已取消
- `WAITING` - 等待中（队列）
- `ERROR` - 出错

### 事件监听

提供详细的下载回调接口：
- `onDownLoadPrepare()` - 准备下载
- `onUpdate()` - 进度更新（进度、已下载、总大小）
- `onDownLoadSuccess()` - 下载成功
- `onDownLoadError()` - 下载失败（含错误信息）
- `onDownLoadPause()` - 下载暂停
- `onDownLoadCancel()` - 下载取消

## 快速开始

### 1. 初始化

```kotlin
// 在 Application 中初始化
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DownloaderManager.init(this)
    }
}
```

### 2. 基础下载

```kotlin
// 简单下载示例
DownloaderManager.download(
    tag = "video",
    url = "https://example.com/video.mp4",
    reDownload = false,
    listener = object : DownLoadListener {
        override fun onUpdate(key: String, progress: Int, read: Long, count: Long, done: Boolean) {
            // 更新进度
        }

        override fun onDownLoadSuccess(key: String, path: String, size: Long) {
            // 下载成功
        }

        override fun onDownLoadError(key: String, errorMsg: String?, throwable: Throwable?) {
            // 下载失败
        }

        // ... 其他回调
    }
)
```

### 3. 自定义路径和文件名

```kotlin
DownloaderManager.download(
    tag = "custom_file",
    url = "https://example.com/file.zip",
    savePath = "/storage/emulated/0/MyDownloads",
    saveName = "custom_name.zip",
    reDownload = false,
    listener = null
)
```

### 4. 任务控制

```kotlin

// 查询任务
val entry = DownloaderManager.queryDownloadEntry("task_key")

// 暂停任务
DownloaderManager.pause(entry)

// 恢复任务（支持断点续传）
DownloaderManager.resume(entry)

// 取消任务（删除已下载文件）
DownloaderManager.cancel(entry)

// 暂停所有
DownloaderManager.pauseAll()

// 恢复所有
DownloaderManager.recoverAll()
```

### 5. 使用 Flow 监听状态

```kotlin
lifecycleScope.launch {
    DownloaderManager.getObserverFlow().collectLatest { entry ->
        when(entry.status) {
            DownloadStatus.DOWNLOADING -> {
                // 更新 UI
            }
            DownloadStatus.COMPLETED -> {
                // 处理完成
            }
            // ... 其他状态
        }
    }
}
```

## 配置选项

通过 `DownloadConfig` 对象配置下载行为：

```kotlin

// 设置最大同时下载数（默认 3）
DownloadConfig.setMaxDownloadTasks(5)

// 设置每个任务的最大线程数（默认 3）
DownloadConfig.setMaxDownloadThreads(5)

// 设置下载目录（默认系统 Download 目录）
DownloadConfig.setDownloadDir("/custom/download/path")

// 设置进度更新最小间隔（默认 1000ms）
DownloadConfig.setMinOperateInterval(500L)

// 设置最大重试次数（默认 3）
DownloadConfig.setMaxRetryTimes(5)

// 设置启动时是否自动恢复下载（默认 false）
DownloadConfig.setRecoverDownloadWhenStart(true)
```

## 截图展示

<p align="center">
 <img width="300" height="auto" src="https://github.com/wuxaye/Xdownloader/blob/master/assets/single.png" >  <img width="300" height="auto" src="https://github.com/wuxaye/Xdownloader/blob/master/assets/multi.png" >
</p>

## 数据库设计

使用 Room 数据库持久化下载信息，存储内容包括：
- 任务基本信息（URL、文件名、状态）
- 下载进度（当前大小、总大小、百分比）
- 断点续传信息（线程进度记录）
- 异常信息（错误类型、错误消息）

## 已知问题

发现下载出现网络异常后，再进行重新下载，下载完成的文件状态可能显示为下载中。

## 待办事项

- [ ] 修复网络异常后重新下载的状态问题
- [ ] 添加下载速度显示
- [ ] 支持优先级任务队列
- [ ] 添加通知栏下载进度显示

## 许可证

欢迎 Star 和 Fork，共同完善这个项目！



