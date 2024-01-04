/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.extensions.getCreatedAtOrThrow
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper
import com.getstream.sdk.chat.viewmodel.messages.MessageListItemLiveData
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.state.DeletedMessageVisibility
import io.getstream.chat.android.common.state.MessageFooterVisibility
import io.getstream.chat.android.test.createDate
import io.getstream.chat.android.test.getOrAwaitValue
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldNotBeEmpty
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

internal class MessageListItemLiveDataTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val currentUser = MutableLiveData(randomUser())

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
        val deletedMessageVisibility = MutableLiveData(DeletedMessageVisibility.ALWAYS_VISIBLE)
        val messageFooterVisibility = MutableLiveData<MessageFooterVisibility>(MessageFooterVisibility.LastInGroup)

        return MessageListItemLiveData(
            currentUser = currentUser,
            messages = messages,
            readsLd = reads,
            typingLd = typing,
            isThread = false,
            dateSeparatorHandler = ::simpleDateGroups,
            deletedMessageVisibility = deletedMessageVisibility,
            messageFooterVisibility = messageFooterVisibility,
            messagePositionHandlerProvider = MessageListViewModel.MessagePositionHandler::defaultHandler,
            members = channelState.members.asLiveData(),
        )
    }

    private fun oneMessage(message: Message): MessageListItemLiveData {
        val messages: LiveData<List<Message>> = MutableLiveData(listOf(message))
        val reads: LiveData<List<ChannelUserRead>> = MutableLiveData(listOf())
        val typing: LiveData<List<User>> = MutableLiveData(listOf())
        val deletedMessageVisibility = MutableLiveData(DeletedMessageVisibility.ALWAYS_VISIBLE)
        val messageFooterVisibility = MutableLiveData<MessageFooterVisibility>(MessageFooterVisibility.LastInGroup)

        return MessageListItemLiveData(
            currentUser = currentUser,
            messages = messages,
            readsLd = reads,
            typingLd = typing,
            isThread = false,
            dateSeparatorHandler = ::simpleDateGroups,
            deletedMessageVisibility = deletedMessageVisibility,
            messageFooterVisibility = messageFooterVisibility,
            messagePositionHandlerProvider = MessageListViewModel.MessagePositionHandler::defaultHandler,
            members = channelState.members.asLiveData(),
        )
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
        // user 0 read till the end, user 1 read the first message,
        // user 3 read is missing
        val read1 = ChannelUserRead(users[0], messages.last().createdAt)
        val read2 = ChannelUserRead(users[1], messages.first().createdAt)
        val reads: LiveData<List<ChannelUserRead>> = MutableLiveData(listOf(read1, read2))
        val typing: LiveData<List<User>> = MutableLiveData(listOf())
        val deletedMessageVisibility = MutableLiveData(DeletedMessageVisibility.ALWAYS_VISIBLE)
        val messageFooterVisibility = MutableLiveData<MessageFooterVisibility>(MessageFooterVisibility.LastInGroup)

        return MessageListItemLiveData(
            currentUser = currentUser,
            messages = messagesLd,
            readsLd = reads,
            typingLd = typing,
            isThread = false,
            dateSeparatorHandler = ::simpleDateGroups,
            deletedMessageVisibility = deletedMessageVisibility,
            messageFooterVisibility = messageFooterVisibility,
            messagePositionHandlerProvider = MessageListViewModel.MessagePositionHandler::defaultHandler,
            members = channelState.members.asLiveData(),
        )
    }

    // livedata testing
    @Test
    fun `Observe should trigger a recompute`() {
        val many = oneMessage(createMessage())
        val items = many.getOrAwaitValue().items
        items.size shouldBeEqualTo 2
        val empty = emptyMessages()
        val items2 = empty.getOrAwaitValue().items
        items2.size shouldBeEqualTo 0
    }

    // test typing indicator logic:
    @Test
    fun `Should return an empty list`() {
        val messageListItemLd = emptyMessages()
        messageListItemLd.typingChanged(emptyList())
        val items = messageListItemLd.getOrAwaitValue().items
        items.shouldBeEmpty()
    }

    @Test
    fun `Should exclude the current user`() {
        val messageListItemLd = emptyMessages()
        val typing = listOf(currentUser.value!!)
        messageListItemLd.handleTypingUsersChange(typing, currentUser.value!!)
        val items = messageListItemLd.getOrAwaitValue().items
        items.shouldBeEmpty()
    }

    @Test
    fun `Should return only the typing indicator`() {
        val messageListItemLd = emptyMessages()
        messageListItemLd.typingChanged(listOf(randomUser()))
        val items = messageListItemLd.getOrAwaitValue().items
        items.size shouldBeEqualTo 1
        items.last().shouldBeInstanceOf<MessageListItem.TypingItem>()
    }

    @Test
    fun `Should return messages with a typing indicator`() {
        val message = createMessage()
        val messageListItemLd = oneMessage(message)
        messageListItemLd.messagesChanged(listOf(message), currentUser.value!!.id)
        messageListItemLd.typingChanged(listOf(randomUser()))
        val items = messageListItemLd.getOrAwaitValue().items
        items.size shouldBeEqualTo 3
        items.first().shouldBeInstanceOf<MessageListItem.DateSeparatorItem>()
        items[1].shouldBeInstanceOf<MessageListItem.MessageItem>()
        items.last().shouldBeInstanceOf<MessageListItem.TypingItem>()
    }

    // test how we merge read state
    @Test
    fun `Last message should contain the read state`() {
        val messageListItemLd = manyMessages()
        val items = messageListItemLd.getOrAwaitValue().items
        val lastMessage = items.last() as MessageListItem.MessageItem
        lastMessage.messageReadBy.shouldNotBeEmpty()
    }

    @Test
    fun `First message should contain the read state`() {
        val messageListItemLd = manyMessages()
        val items = messageListItemLd.getOrAwaitValue().items
        val messages = items.filterIsInstance<MessageListItem.MessageItem>()
        val firstMessage = messages.first()
        firstMessage.messageReadBy.shouldNotBeEmpty()
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
        topMessages shouldBeEqualTo correctPositions + correctPositions + correctPositions
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
        separators.size shouldBeEqualTo 3
    }

    @Test
    fun `When the user is the only one typing, no broadcast is made`() {
        val messageListItemLd = manyMessages()
        val testObserver: Observer<MessageListItemWrapper> = mock()
        messageListItemLd.observeForever(testObserver)
        messageListItemLd.typingChanged(listOf(currentUser.value!!))
        verify(testObserver, times(4)).onChanged(any())
    }

    @Test
    fun `Given regular message followed by system message When grouping messages Should add bottom position to the regular message`() {
        val user = randomUser()
        val messages = listOf(
            createMessage(
                user = user,
                type = ModelType.message_regular,
                createdAt = createDate(year = 2020, month = 11, date = 1, seconds = 1)
            ),
            createMessage(
                user = user,
                type = ModelType.message_system,
                createdAt = createDate(year = 2020, month = 11, date = 1, seconds = 2)
            )
        )

        val messageListItems = emptyMessages().messagesChanged(messages, currentUser.value!!.id).items

        messageListItems[0].shouldBeInstanceOf<MessageListItem.DateSeparatorItem>()
        val regularMessageItem = messageListItems[1]
        regularMessageItem.shouldBeInstanceOf<MessageListItem.MessageItem>()
        regularMessageItem as MessageListItem.MessageItem
        regularMessageItem.positions shouldContain MessageListItem.Position.BOTTOM
    }
}
