package io.getstream.chat.android.client.parser

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.junit.Assert
import org.junit.Test
import java.io.StringReader
import java.io.StringWriter
import java.util.Date

internal class DateAdapterTest {

    private val dateAdapter = DateAdapter()

    @Test
    fun readTest() {
        Assert.assertEquals(1593411268000, dateAdapter.read(JsonReader(StringReader("\"2020-06-29T06:14:28Z\"")))?.time)
        Assert.assertEquals(1593411268000, dateAdapter.read(JsonReader(StringReader("\"2020-06-29T06:14:28.0Z\"")))?.time)
        Assert.assertEquals(1593411268000, dateAdapter.read(JsonReader(StringReader("\"2020-06-29T06:14:28.00Z\"")))?.time)
        Assert.assertEquals(1593411268000, dateAdapter.read(JsonReader(StringReader("\"2020-06-29T06:14:28.000Z\"")))?.time)
        Assert.assertEquals(1593411268100, dateAdapter.read(JsonReader(StringReader("\"2020-06-29T06:14:28.100Z\"")))?.time)
        Assert.assertNull(dateAdapter.read(JsonReader(StringReader(""))))
    }

    @Test
    fun writeTest() {
        var stringWriter = StringWriter()
        var jsonWriter = JsonWriter(stringWriter)
        dateAdapter.write(jsonWriter, Date(1593411268000))
        Assert.assertEquals("\"2020-06-29T06:14:28.000Z\"", stringWriter.toString())

        stringWriter = StringWriter()
        jsonWriter = JsonWriter(stringWriter)
        dateAdapter.write(jsonWriter, null)
        Assert.assertEquals("null", stringWriter.toString())
    }
}
