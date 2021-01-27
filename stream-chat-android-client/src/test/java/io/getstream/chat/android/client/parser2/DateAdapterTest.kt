package io.getstream.chat.android.client.parser2

import org.junit.Assert
import org.junit.Test
import java.util.Date

internal class DateAdapterTest {

    private val dateAdapter = DateAdapter()

    @Test
    fun readValidDates() {
        Assert.assertEquals(1593411268000, dateAdapter.fromJson("\"2020-06-29T06:14:28Z\"")!!.time)
        Assert.assertEquals(1593411268000, dateAdapter.fromJson("\"2020-06-29T06:14:28.0Z\"")!!.time)
        Assert.assertEquals(1593411268000, dateAdapter.fromJson("\"2020-06-29T06:14:28.00Z\"")!!.time)
        Assert.assertEquals(1593411268000, dateAdapter.fromJson("\"2020-06-29T06:14:28.000Z\"")!!.time)
        Assert.assertEquals(1593411268100, dateAdapter.fromJson("\"2020-06-29T06:14:28.100Z\"")!!.time)
    }

    @Test
    fun readEmptyDate() {
        Assert.assertNull(dateAdapter.fromJson("\"\""))
    }

    @Test
    fun readNullDate() {
        Assert.assertNull(dateAdapter.fromJson("null"))
    }

    @Test
    fun readNonsenseDate() {
        Assert.assertNull(dateAdapter.fromJson("\"bla bla bla\""))
    }

    @Test
    fun writeValidDate() {
        val result = dateAdapter.toJson(Date(1593411268000))
        Assert.assertEquals("\"2020-06-29T06:14:28.000Z\"", result)
    }

    @Test
    fun writeNullValue() {
        val result = dateAdapter.toJson(null)
        Assert.assertEquals("null", result)
    }
}
