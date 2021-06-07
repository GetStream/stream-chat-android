package com.getstream.sdk.chat.viewmodel.messages

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.createMessage
import com.getstream.sdk.chat.randomUser
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.test.createDate
import io.getstream.chat.android.test.getOrAwaitValue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

internal class MessageListItemLiveDataTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val currentUser = randomUser()
    private val currentUserLd = MutableLiveData(randomUser())

    private fun simpleDateGroups(previous: Message?, message: Message): Boolean {
        return if (previous == null) {
            true
        } else {
            !isSameDay(message.getCreatedAtOrThrow(), previous.getCreatedAtOrThrow())
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val localDate1: LocalDate = date1.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val localDate2: LocalDate = date2.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return localDate1.isEqual(localDate2)
    }

    private fun emptyMessages(): MessageListItemLiveData {
        val messages: LiveData<List<Message>> = MutableLiveData(listOf())
        val reads: LiveData<List<ChannelUserRead>> = MutableLiveData(listOf())
        val typing: LiveData<List<User>> = MutableLiveData(listOf())

        return MessageListItemLiveData(currentUserLd, messages, reads, typing, false, ::simpleDateGroups)
    }

    private fun oneMessage(message: Message): MessageListItemLiveData {
        val messages: LiveData<List<Message>> = MutableLiveData(listOf(message))
        val reads: LiveData<List<ChannelUserRead>> = MutableLiveData(listOf())
        val typing: LiveData<List<User>> = MutableLiveData(listOf())

        return MessageListItemLiveData(currentUserLd, messages, reads, typing, false, ::simpleDateGroups)
    }

    private fun manyMessages(): MessageListItemLiveData {
        val messages = mutableListOf<Message>()

        val users = listOf(randomUser(), randomUser(), randomUser())

        for (i in (0..2)) {
            val user = users[i]
            for (y in 0..2) {
                val message = createMessage(user = user, createdAt = createDate(2020, 11, i + 1, i + y))
                messages.add(message)
            }
        }
        val messagesLd: LiveData<List<Message>> = MutableLiveData(messages)
        // user 0 read till the end, user 1 read the first message, user 3 read is missing
        val read1 = ChannelUserRead(users[0], messages.last().createdAt)
        val read2 = ChannelUserRead(users[1], messages.first().createdAt)
        val reads: LiveData<List<ChannelUserRead>> = MutableLiveData(listOf(read1, read2))
        val typing: LiveData<List<User>> = MutableLiveData(listOf())

        return MessageListItemLiveData(currentUserLd, messagesLd, reads, typing, false, ::simpleDateGroups)
    }

    // livedata testing
    @Test
    fun `Observe should trigger a recompute`() {
        val many = oneMessage(createMessage())
        val items = many.getOrAwaitValue().items
        Truth.assertThat(items.size).isEqualTo(2)
        val empty = emptyMessages()
        val items2 = empty.getOrAwaitValue().items
        Truth.assertThat(items2.size).isEqualTo(0)
    }

    // test typing indicator logic:
    @Test
    fun `Should return an empty list`() {
        val messageListItemLd = emptyMessages()
        messageListItemLd.typingChanged(emptyList())
        val items = messageListItemLd.getOrAwaitValue().items
        Truth.assertThat(items).isEmpty()
    }

    @Test
    fun `Should exclude the current user`() {
        val messageListItemLd = emptyMessages()
        val typing = listOf(currentUser)
        messageListItemLd.typingChanged(typing)
        val items = messageListItemLd.getOrAwaitValue().items
        Truth.assertThat(items).isEmpty()
    }

    @Test
    fun `Should return only the typing indicator`() {
        val messageListItemLd = emptyMessages()
        messageListItemLd.typingChanged(listOf(randomUser()))
        val items = messageListItemLd.getOrAwaitValue().items
        Truth.assertThat(items.size).isEqualTo(1)
        Truth.assertThat(items.last()).isInstanceOf(MessageListItem.TypingItem::class.java)
    }

    @Test
    fun `Should return messages with a typing indicator`() {
        val message = createMessage()
        val messageListItemLd = oneMessage(message)
        messageListItemLd.messagesChanged(listOf(message), currentUser.id)
        messageListItemLd.typingChanged(listOf(randomUser()))
        val items = messageListItemLd.getOrAwaitValue().items
        Truth.assertThat(items.size).isEqualTo(3)
        Truth.assertThat(items.first()).isInstanceOf(MessageListItem.DateSeparatorItem::class.java)
        Truth.assertThat(items[1]).isInstanceOf(MessageListItem.MessageItem::class.java)
        Truth.assertThat(items.last()).isInstanceOf(MessageListItem.TypingItem::class.java)
    }

    // test how we merge read state
    @Test
    fun `Last message should contain the read state`() {
        val messageListItemLd = manyMessages()
        val items = messageListItemLd.getOrAwaitValue().items
        val lastMessage = items.last() as MessageListItem.MessageItem
        Truth.assertThat(lastMessage.messageReadBy).isNotEmpty()
    }

    @Test
    fun `First message should contain the read state`() {
        val messageListItemLd = manyMessages()
        val items = messageListItemLd.getOrAwaitValue().items
        val messages = items.filterIsInstance<MessageListItem.MessageItem>()
        val firstMessage = messages.first()
        Truth.assertThat(firstMessage.messageReadBy).isNotEmpty()
    }

    // test message grouping
    @Test
    fun `There should be 3 messages with a position Top`() {
        val messageListItemLd = manyMessages()
        val topMessages = mutableListOf<MessageListItem.Position>()
        val items = messageListItemLd.getOrAwaitValue().items
        for (item in items) {
            if (item is MessageListItem.MessageItem) {
                val messageItem = item
                topMessages.addAll(messageItem.positions)
            }
        }
        // there are 3 users, so we should have 3 top sections
        val correctPositions =
            listOf(MessageListItem.Position.TOP, MessageListItem.Position.MIDDLE, MessageListItem.Position.BOTTOM)
        Truth.assertThat(topMessages).isEqualTo(correctPositions + correctPositions + correctPositions)
    }

    // test data separators
    @Test
    fun `There should be 3 date separators`() {
        val messageListItemLd = manyMessages()
        val separators = mutableListOf<MessageListItem.DateSeparatorItem>()
        val items = messageListItemLd.getOrAwaitValue().items
        for (item in items) {
            if (item is MessageListItem.DateSeparatorItem) {
                separators.add(item)
            }
        }
        Truth.assertThat(separators.size).isEqualTo(3)
    }

    @Test
    fun `When the user is the only one typing, no broadcast is made`() {
        val messageListItemLd = manyMessages()
        val testObserver: Observer<MessageListItemWrapper> = mock()
        messageListItemLd.observeForever(testObserver)
        messageListItemLd.typingChanged(listOf(currentUser))
        verify(testObserver, times(2)).onChanged(any())
    }
}
