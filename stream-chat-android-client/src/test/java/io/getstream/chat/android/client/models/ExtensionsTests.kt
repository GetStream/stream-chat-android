package io.getstream.chat.android.client.models

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

internal class ExtensionsTests {

    @Test
    fun noUnread() {
        Channel().getUnreadMessagesCount() shouldBeEqualTo 0
    }

    @Test
    fun totalUnread() {
        Channel(read = listOf(getRead(10))).getUnreadMessagesCount() shouldBeEqualTo 10

        Channel(
            read = listOf(
                getRead(10),
                getRead(10)
            )
        ).getUnreadMessagesCount() shouldBeEqualTo 20
    }

    @Test
    fun unreadByUsers() {

        val userA = "user-a"
        val userB = "user-b"
        val unreadUserA = 10
        val unreadUserB = 5

        val channel = Channel(
            read = listOf(
                getRead(unreadUserA, userA),
                getRead(unreadUserB, userB)
            )
        )

        channel.getUnreadMessagesCount() shouldBeEqualTo unreadUserA + unreadUserB
        channel.getUnreadMessagesCount(userA) shouldBeEqualTo unreadUserA
        channel.getUnreadMessagesCount(userB) shouldBeEqualTo unreadUserB
    }

    private fun getRead(unreadCount: Int, userId: String = ""): ChannelUserRead {
        return ChannelUserRead(User(id = userId), null, unreadCount)
    }
}
