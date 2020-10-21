package io.getstream.chat.android.livedata.converter

import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseTest
import org.junit.Test

internal class ConfigConverterTest : BaseTest() {

    @Test
    fun testSortEncoding() {
        val converter = ConfigConverter()
        val output = converter.channelConfigToString(data.config1)
        val converted = converter.stringToChannelConfig(output)
        Truth.assertThat(converted!!).isEqualTo(converted)
    }
}
