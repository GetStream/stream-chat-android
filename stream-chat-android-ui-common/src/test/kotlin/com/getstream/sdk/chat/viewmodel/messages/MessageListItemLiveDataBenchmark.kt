package com.getstream.sdk.chat.viewmodel.messages

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.createMessage
import com.getstream.sdk.chat.randomUser
import com.google.common.truth.Truth
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.test.createDate
import io.getstream.chat.android.test.getOrAwaitValue
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import kotlin.system.measureTimeMillis

@Ignore("Flaky on CI")
internal class MessageListItemLiveDataBenchmark {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val currentUser = randomUser()

    private val threeHours = 1000 * 60 * 60 * 3

    private fun simpleDateGroups(previous: Message?, message: Message): Boolean {
        return if (previous == null) {
            true
        } else {
            (message.getCreatedAtOrThrow().time - previous.getCreatedAtOrThrow().time) > threeHours
        }
    }

    private fun manyMessages(): MessageListItemLiveData {
        val messages = mutableListOf<Message>()

        for (i in (0..5)) {
            val user = randomUser()
            for (y in 0..50) {
                val message = createMessage(user = user, createdAt = createDate(2020, 11, i % 28 + 1, 1, i, y))
                messages.add(message)
            }
        }
        val messagesLd: LiveData<List<Message>> = MutableLiveData(messages)
        // user 0 read till the end, user 1 read the first message, user 3 read is missing
        val read1 = ChannelUserRead(messages.first().user, messages.last().createdAt)
        val read2 = ChannelUserRead(messages[10].user, messages.first().createdAt)
        val reads: LiveData<List<ChannelUserRead>> = MutableLiveData(listOf(read1, read2))
        val typing: LiveData<List<User>> = MutableLiveData(listOf())

        return MessageListItemLiveData({ currentUser.id }, messagesLd, reads, typing, false, ::simpleDateGroups)
    }

    @Test
    fun `the most frequent change to the message list is typing changes`() {
        val messageLd = manyMessages()
        val items = messageLd.getOrAwaitValue().items

        val duration = measureTimeMillis {
            for (x in 0..50) {
                messageLd.typingChanged(listOf(User(id = x.toString())))
                messageLd.typingChanged(emptyList())
            }
        }
        println("changing typing information 100 times on a message list with ${items.size} items took $duration milliseconds")
        Truth.assertThat(duration).isLessThan(25)
    }

    @Test
    fun `the second most frequent change is read state changes`() {
        val messageLd = manyMessages()
        val items = messageLd.getOrAwaitValue().items

        val users = items.filterIsInstance<MessageListItem.MessageItem>().map { it.message.user }.distinct()
        val messages = items.filterIsInstance<MessageListItem.MessageItem>().map { it.message.createdAt }.takeLast(5)
        val reads = users.map { ChannelUserRead(it, messages.random()) }

        val duration = measureTimeMillis {
            for (x in 0..100) {
                messageLd.readsChanged(reads)
            }
        }
        println("changing read information 100 times on a message list with ${items.size} items took $duration milliseconds")
        Truth.assertThat(duration).isLessThan(50)
    }

    @Test
    fun `new messages dont happen as often`() {
        val messageLd = manyMessages()
        val items = messageLd.getOrAwaitValue().items

        val messages = items.filterIsInstance<MessageListItem.MessageItem>().map { it.message }

        val duration = measureTimeMillis {
            for (x in 0..100) {
                val newMessages = messages + createMessage()
                messageLd.messagesChanged(newMessages)
            }
        }
        println("changing messages 100 times on a message list with ${items.size} items took $duration milliseconds")
        Truth.assertThat(duration).isLessThan(250)
    }
}
