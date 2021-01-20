package io.getstream.chat.android.livedata

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.utils.computeUnreadCount
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ComputeUnreadCountTest : BaseDomainTest2() {

    private val validUserRead = ChannelUserRead(data.user1, data.message1.createdAt, 2)

    @Test
    fun `when messages are present, but read is null, return valid messages size`() {
        val count = computeUnreadCount(
            currentUser = data.user1,
            messages = listOf(
                data.message1, // ignored because it's from the current user
                data.messageFromUser2,
                data.messageFromUser2
            )
        )
        Truth.assertThat(count).isEqualTo(2)
    }

    @Test
    fun `when message count is 1 or fewer and read is present, return unread count from read`() {
        val count = computeUnreadCount(
            currentUser = data.user1,
            read = validUserRead,
            messages = listOf(
                data.message1.copy(createdAt = null), // would throw if not ignored
                data.messageFromUser2
            )
        )

        // If the read includes 2, and backend behavior is to give 1 message, that message
        // is part of the unread count. The message from the current user is ignored, so
        // we have 1 valid message. That triggers the logic return the read's unread count.
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
            // needs more than 1 valid message, otherwise the read's unread count is returned.
            messages = listOf(
                data.message1,
                data.messageFromUser2,
                data.messageFromUser2.copy(createdAt = null)
            )
        )
    }

    @Test
    fun `should ignore messages from current user when computing unread count`() {
        val count = computeUnreadCount(
            currentUser = data.user1,
            messages = listOf(
                data.message1.copy(createdAt = null), // would throw if not ignored 
            )
        )

        Truth.assertThat(count).isEqualTo(0)
    }

    @Test
    fun `should return correct count of unread messages`() {
        val count = computeUnreadCount(
            currentUser = data.user1,
            read = validUserRead,
            messages = listOf(
                data.messageFromUser2,
                data.messageFromUser2,
                data.messageFromUser2,
            )
        )
        // our read indicates 2 unread from the backend, we count 3, minus 1 because one message
        // is included in the backend's calculations, means we have 4 total unread, whether we
        // have all of them locally or not.
        Truth.assertThat(count).isEqualTo(4)
    }
}
