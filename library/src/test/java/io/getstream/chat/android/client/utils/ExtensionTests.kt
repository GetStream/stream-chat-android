package io.getstream.chat.android.client.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExtensionTests {
    @Test
    fun testContainsKeys() {
        val map = HashMap<String, String>()
        assertThat(map.containsKeys("a", "b", "c")).isFalse()

        map["a"] = "a"

        assertThat(map.containsKeys("a", "b", "c")).isFalse()
        assertThat(map.containsKeys("a")).isTrue()

        map["b"] = "b"
        map["c"] = "c"

        assertThat(map.containsKeys("a", "b", "c")).isTrue()
        assertThat(map.containsKeys("a", "b", "c", "d")).isFalse()
    }
}