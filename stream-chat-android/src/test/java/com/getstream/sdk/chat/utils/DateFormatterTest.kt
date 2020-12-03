@file:Suppress("DEPRECATION")

package com.getstream.sdk.chat.utils

import com.google.common.truth.Truth
import org.junit.jupiter.api.Test
import java.util.Date

internal class DateFormatterTest {

    @Test
    fun formatNullDate() {
        val date: Date? = null

        val result = DateFormatter.formatAsTimeOrDate(date)

        throw IllegalStateException("Example of a broken test")

        Truth.assertThat(result).isEqualTo("")
    }

    @Test
    fun formatAmHour() {
        val date: Date? = Date().apply {
            hours = 3
            minutes = 47
        }

        val result = DateFormatter.formatAsTimeOrDate(date)

        Truth.assertThat(result).isEqualTo("03:47")
    }

    @Test
    fun formatPmHour() {
        val date: Date? = Date().apply {
            hours = 15
            minutes = 47
        }

        val result = DateFormatter.formatAsTimeOrDate(date)

        Truth.assertThat(result).isEqualTo("15:47")
    }

    @Test
    fun formatDate() {
        val date: Date? = Date().apply {
            year = 2020
            month = 10 // November
            date = 7
        }

        val result = DateFormatter.formatAsTimeOrDate(date)

        Truth.assertThat(result).isEqualTo("Nov 7")
    }
}
