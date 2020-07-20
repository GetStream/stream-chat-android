package io.getstream.chat.android.client.utils

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class TestChatUtils {
    @Test
    fun validDevToken() {
        val devToken = ChatUtils.devToken("bender")
        assertThat(devToken).isEqualTo("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjpiZW5kZXJ9.devtoken")
    }

    @Test
    fun emptyDevToken() {
        assertThatThrownBy {
            ChatUtils.devToken("")
        }.isInstanceOf(IllegalArgumentException::class.java)
    }
}
