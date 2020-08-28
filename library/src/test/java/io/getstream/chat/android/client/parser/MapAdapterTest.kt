package io.getstream.chat.android.client.parser

import com.google.gson.Gson
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class MapAdapterTest {
    val mapAdapter = MapAdapter(Gson().getAdapter(Map::class.java))

    @Test
    fun `Should render emptyMap as null`() {
        mapAdapter.toJson(emptyMap<Any, Any>()) `should be equal to` "null"
    }

    @Test
    fun `Should omit key with null values`() {
        mapAdapter.toJson(mapOf("a" to "b", "c" to null, "d" to 1)) `should be equal to` "{\"a\":\"b\",\"d\":1}"
    }
}
