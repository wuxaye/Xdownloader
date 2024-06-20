package com.xaye.downloader.utils

internal object TextUtil {

    fun getTimeLeftText(speedInBPerMs: Float, progressPercent: Int, lengthInBytes: Long): String {
        val speedInBPerSecond = speedInBPerMs * 1000
        val bytesLeft = (lengthInBytes * (100 - progressPercent) / 100).toFloat()

        val secondsLeft = bytesLeft / speedInBPerSecond
        val minutesLeft = secondsLeft / 60
        val hoursLeft = minutesLeft / 60

        return when {
            secondsLeft < 60 -> "剩余 %.0f 秒".format(secondsLeft)
            minutesLeft < 3 -> "剩余 %.0f 分 %.0f 秒".format(minutesLeft, secondsLeft % 60)
            minutesLeft < 60 -> "剩余 %.0f 分".format(minutesLeft)
            minutesLeft < 300 -> "剩余 %.0f 时 %.0f 分".format(hoursLeft, minutesLeft % 60)
            else -> "剩余 %.0f 时".format(hoursLeft)
        }
    }

    fun getSpeedText(speedInBPerMs: Float): String {
        var value = speedInBPerMs * 1000
        val units = arrayOf("b/s", "kb/s", "mb/s", "gb/s")
        var unitIndex = 0

        while (value >= 500 && unitIndex < units.size - 1) {
            value /= 1024
            unitIndex++
        }

        return "%.2f %s".format(value, units[unitIndex])
    }

    fun getTotalLengthText(lengthInBytes: Long): String {
        var value = lengthInBytes.toFloat()
        val units = arrayOf("b", "kb", "mb", "gb")
        var unitIndex = 0

        while (value >= 500 && unitIndex < units.size - 1) {
            value /= 1024
            unitIndex++
        }

        return "%.2f %s".format(value, units[unitIndex])
    }

}