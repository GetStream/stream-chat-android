package com.getstream.sdk.chat.viewmodel.messages

import androidx.arch.core.executor.testing.InstantExecutorExtension
import androidx.lifecycle.MutableLiveData
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.createMessage
import com.getstream.sdk.chat.utils.livedata.TestObserver
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class)
class MessageListItemLiveDataTests {

    private val currentUser = User()
    private lateinit var messages: MutableLiveData<List<Message>>
    private lateinit var threadMessages: MutableLiveData<List<Message>>
    private lateinit var typing: MutableLiveData<List<User>>
    private lateinit var reads: MutableLiveData<List<ChannelUserRead>>

    private lateinit var observer: TestObserver<MessageListItemWrapper>

    private lateinit var sut: MessageListItemLiveData

    @BeforeEach
    fun setUp() {
        observer = TestObserver()
        messages = MutableLiveData(emptyList())
        threadMessages = MutableLiveData(emptyList())
        typing = MutableLiveData(emptyList())
        reads = MutableLiveData(emptyList())

        sut = MessageListItemLiveData(currentUser, messages, threadMessages, typing, reads)
    }

    @Test
    fun `When messages are changed if live data is in the thread state should not be observed any values`() {
        sut.observeForever(observer)
        threadMessages.value = listOf(createMessage())
        observer.reset()

        messages.value = listOf(createMessage())

        observer.lastObservedValue shouldBeEqualTo null
    }

    @Test
    fun `When messages are changed if live data is not in the thread state should observe last assigned value to messages`() {
        sut.observeForever(observer)
        val sendingMessage = createMessage()

        messages.value = listOf(sendingMessage)

        val result = observer.lastObservedValue
        result.shouldNotBeNull()
        result.listEntities.any { item ->
            item is MessageListItem.MessageItem && item.message == sendingMessage
        } shouldBeEqualTo true
    }

    @Test
    fun `When thread messages are changed should observe last assigned value to thread messages`() {
        sut.observeForever(observer)
        val threadMessage = createMessage()

        threadMessages.value = listOf(threadMessage)

        val result = observer.lastObservedValue
        result.shouldNotBeNull()
        result.listEntities.any { item ->
            item is MessageListItem.MessageItem && item.message == threadMessage
        } shouldBeEqualTo true
    }

    @Test
    fun `When set thread messages should remove observer from old thread messages and set to new one`() {
        sut.observeForever(observer)
        val newThreadMessages = MutableLiveData<List<Message>>(listOf())

        sut.setThreadMessages(newThreadMessages)

        threadMessages.hasObservers() shouldBeEqualTo false
        newThreadMessages.hasObservers() shouldBeEqualTo true
    }

    @Test
    fun `When set thread messages should observe values from new thread messages`() {
        sut.observeForever(observer)
        val newThreadMessage = createMessage()
        val newThreadMessages = MutableLiveData(listOf(newThreadMessage))

        sut.setThreadMessages(newThreadMessages)

        val result = observer.lastObservedValue
        result.shouldNotBeNull()
        result.listEntities.any { item ->
            item is MessageListItem.MessageItem && item.message == newThreadMessage
        } shouldBeEqualTo true
    }
}
