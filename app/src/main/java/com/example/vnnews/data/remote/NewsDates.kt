package com.example.vnnews.data.remote

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Locale

object NewsDates {
    private val shortYear = DateTimeFormatterBuilder().parseCaseInsensitive()
        .appendPattern("EEE, dd MMM ")
        .appendValueReduced(ChronoField.YEAR, 2, 2, 1970)
        .appendPattern(" HH:mm:ss Z").toFormatter(Locale.US)
    private val local = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a", Locale.US)

    fun parse(value: String?): Instant? {
        val text = value?.replace('\u202f', ' ')?.replace('\u00a0', ' ')?.trim() ?: return null
        for (format in listOf(shortYear, DateTimeFormatter.RFC_1123_DATE_TIME)) {
            runCatching { ZonedDateTime.parse(text, format).toInstant() }.getOrNull()?.let { return it }
        }
        return runCatching { LocalDateTime.parse(text, local).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant() }.getOrNull()
    }
}
