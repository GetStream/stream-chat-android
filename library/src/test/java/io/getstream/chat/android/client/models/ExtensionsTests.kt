package io.getstream.chat.android.client.models

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExtensionsTests {
    @Test
    fun unreadCountOfChannel() {
        assertThat(Channel().getUnreadMessagesCount()).isEqualTo(0)

        assertThat(Channel(read = listOf(getRead(10))).getUnreadMessagesCount()).isEqualTo(10)

        assertThat(
            Channel(
                read = listOf(
                    getRead(10),
                    getRead(10)
                )
            ).getUnreadMessagesCount()
        ).isEqualTo(20)
    }

    private fun getRead(unreadCount: Int, userId: String = ""): ChannelUserRead {
        return ChannelUserRead(User(id = userId), null, unreadCount)
    }
}