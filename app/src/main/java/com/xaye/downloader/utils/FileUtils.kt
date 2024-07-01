package com.xaye.downloader.utils

import java.io.File
import java.io.UnsupportedEncodingException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and

/**
 * @FileName:com.xaye.downloader.utilities.FileUtils.kt
 * Author xaye
 * @date: 2024-05-12 21:00
 * Created by 11623 on 2024/5/12
 */
internal object FileUtils {
    private val HASH_ALGORITHM = "MD5"
    private val RADIX = 10 + 26

    //生成一个基于给定 URL 的 MD5 文件名，保证每个文件名的唯一性
    fun getMd5FileName(url: String) : String {
        val md5 = getMd5(url.toByteArray())
        val bi = BigInteger(md5).abs()
        return bi.toString(RADIX) + url.substring(url.lastIndexOf('/') + 1)
    }

    private fun getMd5(data: ByteArray): ByteArray? {
        var hash: ByteArray? = null
        try {
            val algorithm = MessageDigest.getInstance(HASH_ALGORITHM)
            algorithm.update(data)
            hash = algorithm.digest()
        } catch (e: NoSuchAlgorithmException) {
            Trace.e(e.message?:"")
        }
        return hash
    }

    /**
     * 返回一个整数表示该组合的唯一标识符
     */
    fun getUniqueId(url: String, dirPath: String, fileName: String): Int {
        val string = url + File.separator + dirPath + File.separator + fileName
        val hash: ByteArray = try {
            MessageDigest.getInstance(HASH_ALGORITHM).digest(string.toByteArray(charset("UTF-8")))
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("NoSuchAlgorithmException", e)
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException("UnsupportedEncodingException", e)
        }
        val hex = StringBuilder(hash.size * 2)
        for (b in hash) {
            if (b and 0xFF.toByte() < 0x10) hex.append("0")
            hex.append(Integer.toHexString((b and 0xFF.toByte()).toInt()))
        }
        return hex.toString().hashCode()
    }

}