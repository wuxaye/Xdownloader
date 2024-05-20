package com.xaye.downloader.utilities;

/**
 * @FileName:com.xaye.downloader.Constants.kt Author xaye
 * @date: 2024-04-05 08:55
 * Created by 11623 on 2024/4/5
 */
public class Constants {

    public static final String KEY_DOWNLOAD_ENTRY = "key_download_entry";
    public static final String KEY_DOWNLOAD_ACTION = "key_download_action";
    public static final int KEY_DOWNLOAD_ACTION_ADD = 0x01;
    public static final int KEY_DOWNLOAD_ACTION_PAUSE = 0x02;
    public static final int KEY_DOWNLOAD_ACTION_RESUME = 0x03;
    public static final int KEY_DOWNLOAD_ACTION_CANCEL = 0x04;
    public static final int KEY_DOWNLOAD_ACTION_PAUSE_ALL = 0x05;
    public static final int KEY_DOWNLOAD_ACTION_RECOVER_ALL = 0x06;
    public static final int CONNECT_TIMEOUT = 3000;//连接超时时间
    public static final int READ_TIMEOUT = 5000;//读取超时时间
}
