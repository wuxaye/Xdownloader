package com.xaye.downloader.network

/**
 * Author xaye
 * @date: 2024/5/31
 */
enum class Error(private val code: Int, private val err: String) {
    /**
     * 未知错误
     */
    UNKNOWN(1000, "请求失败，请稍后再试"),
    /**
     * 解析错误
     */
    PARSE_ERROR(1001, "解析错误，请稍后再试"),
    /**
     * 网络错误
     */
    NETWORK_ERROR(1002, "网络连接错误，请稍后重试"),

    /**
     * 证书出错
     */
    SSL_ERROR(1004, "证书出错，请稍后再试"),

    /**
     * 连接超时
     */
    TIMEOUT_ERROR(1006, "网络连接超时，请稍后重试"),

    /**
     * 手动取消
     */
    MANUALLY_ERROR(1007, "手动取消"),

    /**
     * 服务器出错
     */
    SERVER_ERROR(1008, "服务器出错，请稍后再试"),

    /**
     * 建立连接时 服务器响应码不对
     */
    RESPONSE_CODE_ERROR(1009, "响应码错误");

    fun getValue(): String {
        return err
    }

    fun getKey(): Int {
        return code
    }
}