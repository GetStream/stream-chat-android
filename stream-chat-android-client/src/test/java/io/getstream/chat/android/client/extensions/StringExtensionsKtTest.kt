package io.getstream.chat.android.client.extensions

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class StringExtensionsKtTest {

    @Test
    fun `given a String is lower cammel case, it should be parsed to getter`() {
        val text = "cammelCase"
        val expected = "getCammelCase"

        text.lowerCamelCaseToGetter() `should be equal to` expected
    }

}
