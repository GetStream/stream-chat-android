package com.getstream.sdk.chat.utils

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.util.Date

internal class LegacyDateFormatterTest {

    class TestDateFormatter : LegacyDateFormatter() {
        var lastParam: Date? = null

        override fun formatDate(date: Date?): String {
            lastParam = date
            return ""
        }

        override fun formatTime(time: Date?): String {
            lastParam = time
            return ""
        }
    }

    @Test
    fun `LegacyDateFormatter converts LocalDateTime to Date correctly`() {
        val localDateTime = LocalDateTime.of(2020, 4, 19, 15, 50, 37)

        val legacyFormatter = TestDateFormatter()
        legacyFormatter.formatDate(localDateTime)

        val expected = Date(
            120, // year, starts from 1900
            3, // month, 0-indexed
            19, // date
            15, // hours
            50, // minutes
            37, // seconds
        )
        legacyFormatter.lastParam shouldBeEqualTo expected
    }

    @Test
    fun `LegacyDateFormatter converts times to Date correctly`() {
        val localTime = LocalTime.of(15, 50, 37)

        val legacyFormatter = TestDateFormatter()
        legacyFormatter.formatTime(localTime)

        val expected = Date(0L).apply {
            hours = 15
            minutes = 50
            seconds = 37
        }
        legacyFormatter.lastParam shouldBeEqualTo expected
    }
}
