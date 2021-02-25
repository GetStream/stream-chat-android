package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.parser2.adapters.DateAdapter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.Date

internal class DateAdapterTest {

    private val dateAdapter = DateAdapter()

    @Test
    fun readValidDates() {
        assertEquals(1593411268000, dateAdapter.fromJson("\"2020-06-29T06:14:28Z\"")!!.time)
        assertEquals(1593411268000, dateAdapter.fromJson("\"2020-06-29T06:14:28.0Z\"")!!.time)
        assertEquals(1593411268000, dateAdapter.fromJson("\"2020-06-29T06:14:28.00Z\"")!!.time)
        assertEquals(1593411268000, dateAdapter.fromJson("\"2020-06-29T06:14:28.000Z\"")!!.time)
        assertEquals(1593411268100, dateAdapter.fromJson("\"2020-06-29T06:14:28.100Z\"")!!.time)
    }

    @Test
    fun readEmptyDate() {
        assertNull(dateAdapter.fromJson("\"\""))
    }

    @Test
    fun readNullDate() {
        assertNull(dateAdapter.fromJson("null"))
    }

    @Test
    fun readNonsenseDate() {
        assertNull(dateAdapter.fromJson("\"bla bla bla\""))
    }

    @Test
    fun writeValidDate() {
        val result = dateAdapter.toJson(Date(1593411268000))
        assertEquals("\"2020-06-29T06:14:28.000Z\"", result)
    }

    @Test
    fun writeNullValue() {
        val result = dateAdapter.toJson(null)
        assertEquals("null", result)
    }
}
