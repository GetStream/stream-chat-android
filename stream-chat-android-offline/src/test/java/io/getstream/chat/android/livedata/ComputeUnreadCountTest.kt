package io.getstream.chat.android.livedata

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.utils.computeUnreadCount
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ComputeUnreadCountTest : BaseDomainTest2() {

    companion object {
        const val USER_READ_UNREAD_COUNT: Int = 2
    }

    // lateinit var currentUser: User

    val validUserRead = ChannelUserRead(data.user1, data.message1.createdAt, USER_READ_UNREAD_COUNT)

    @Before
    fun before() {
        // userRead = ChannelUserRead(currentUser, messages.last().createdAt, USER_READ_UNREAD_COUNT)
    }

    @Test
    fun `when messages are null, unread count should be null`() {
        val count = computeUnreadCount(data.user1)
        Truth.assertThat(count).isNull()
    }

    // @Test
    // fun `when messages are empty, return 0`() {
    //     val count = computeUnreadCount(data.user1, messages = emptyList())
    //     Truth.assertThat(count).isEqualTo(0)
    // }

    @Test
    fun `when messages are present, but read is null, return messages size`() {
        val count = computeUnreadCount(data.user1, messages = listOf(data.message1))
        Truth.assertThat(count).isEqualTo(1)
    }

    @Test
    fun `when message count is 1 or fewer and read is present, return unread count from read`() {
        val count = computeUnreadCount(
            currentUser = data.user1,
            read = validUserRead,
            messages = emptyList()
        )
        Truth.assertThat(count).isEqualTo(validUserRead.unreadMessages)
    }

    @Test
    fun `when read is null, unread count should be the number of messages`() {
        val messages = emptyList<Message>()
        val count = computeUnreadCount(data.user1, messages = messages)
        Truth.assertThat(count).isEqualTo(0)
    }

    @Test(expected = IllegalStateException::class)
    fun `when read date is missing, should throw exception`() {
        computeUnreadCount(
            currentUser = data.user1,
            read = validUserRead.copy(lastRead = null),
            messages = listOf(data.message1, data.messageFromUser2)
        )
    }

    @Test(expected = IllegalStateException::class)
    fun `when message's creation date is missing, should throw illegal state exception`() {
        computeUnreadCount(
            currentUser = data.user1,
            read = validUserRead,
            messages = listOf(data.message1, data.messageFromUser2.copy(createdAt = null))
        )
    }

    // @Test
    // fun `when message count is one or fewer, THEN unread count should be the read's unread count`() {
    //     val messages = listOf(data.message1)
    //     val read = ChannelUserRead(data.user1, unreadMessages = USER_READ_UNREAD_COUNT)
    //     val count = computeUnreadCount(data.user1, read, messages)
    //     Truth.assertThat(count).isEqualTo(read.unreadMessages)
    // }

    @Test
    fun `should ignore messages from current user when computing unread count`() {
        val count = computeUnreadCount(
            currentUser = data.user1,
            read = validUserRead,
            messages = listOf(data.message1.copy(createdAt = null), data.messageFromUser2) // would throw if not ignored
        )

        Truth.assertThat(count).isEqualTo(1)
    }

    @Test
    fun `should return correct count of unread messages`() {
        val count = computeUnreadCount(
            currentUser = data.user1,
            read = validUserRead,
            messages = listOf(
                data.message1,
                data.messageFromUser2,
                data.messageFromUser2,
                data.messageFromUser2,
                data.message2Older
            )
        )

        Truth.assertThat(count).isEqualTo(3)
    }
}
