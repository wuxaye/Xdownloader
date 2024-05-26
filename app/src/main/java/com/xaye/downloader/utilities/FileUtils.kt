package com.xaye.downloader.utilities

import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * @FileName:com.xaye.downloader.utilities.FileUtils.kt
 * Author xaye
 * @date: 2024-05-12 21:00
 * Created by 11623 on 2024/5/12
 */
class FileUtils {
    companion object {
        private val HASH_ALGORITHM = "MD5"
        private val RADIX = 10 + 26

        //生成一个基于给定 URL 的 MD5 文件名
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
    }

}