package com.llmusage.monitor.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * All "day" math throughout the app uses UTC epoch-day to avoid timezone
 * drift between background WorkManager runs and foreground use.
 */
object TimeUtils {

    private val utc: ZoneId = ZoneId.of("UTC")

    fun todayEpochDay(): Long = LocalDate.now(utc).toEpochDay()

    fun epochDayOf(epochMillis: Long): Long =
        Instant.ofEpochMilli(epochMillis).atZone(utc).toLocalDate().toEpochDay()

    fun localFormat(epochMillis: Long?): String {
        if (epochMillis == null) return "—"
        val fmt = DateTimeFormatter.ofPattern("MMM d, HH:mm").withZone(ZoneId.systemDefault())
        return fmt.format(Instant.ofEpochMilli(epochMillis))
    }

    fun dateLabel(epochDay: Long): String {
        val date = LocalDate.ofEpochDay(epochDay)
        return DateTimeFormatter.ofPattern("MMM d").format(date)
    }
}
