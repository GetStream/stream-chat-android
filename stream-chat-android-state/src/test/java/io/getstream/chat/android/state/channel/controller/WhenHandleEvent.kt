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

package io.getstream.chat.android.state.channel.controller

import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.test.SynchronizedCoroutineTest
import io.getstream.chat.android.client.test.randomChannelDeletedEvent
import io.getstream.chat.android.client.test.randomMemberAddedEvent
import io.getstream.chat.android.client.test.randomMessageReadEvent
import io.getstream.chat.android.client.test.randomMessageUpdateEvent
import io.getstream.chat.android.client.test.randomNewMessageEvent
import io.getstream.chat.android.client.test.randomNotificationMarkReadEvent
import io.getstream.chat.android.client.test.randomPollDeletedEvent
import io.getstream.chat.android.client.test.randomReactionNewEvent
import io.getstream.chat.android.client.test.randomTypingStartEvent
import io.getstream.chat.android.client.test.randomTypingStopEvent
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.state.event.handler.internal.utils.toChannelUserRead
import io.getstream.chat.android.state.message.attachments.internal.AttachmentUrlValidator
import io.getstream.chat.android.state.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.state.plugin.logic.channel.internal.ChannelStateLogic
import io.getstream.chat.android.state.plugin.state.channel.internal.ChannelMutableState
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@ExperimentalCoroutinesApi
internal class WhenHandleEvent : SynchronizedCoroutineTest {

    @get:Rule
    val testCoroutines = TestCoroutineRule()
    private val channelId = randomString()
    private val currentUser = User(id = CURRENT_USER_ID)
    private val userFlow = MutableStateFlow(currentUser)

    override fun getTestScope(): TestScope = testCoroutines.scope

    private val repos: RepositoryFacade = mock()
    private val attachmentUrlValidator: AttachmentUrlValidator = mock()

    private lateinit var channelLogic: ChannelLogic
    private val channelMutableState: ChannelMutableState = ChannelMutableState(
        channelType = "type1",
        channelId = channelId,
        userFlow = userFlow,
        latestUsers = MutableStateFlow(
            mapOf(currentUser.id to currentUser),
        ),
        activeLiveLocations = MutableStateFlow(
            emptyList(),
        ),
    ) { System.currentTimeMillis() }

    private val channelStateLogic: ChannelStateLogic = mock {
        on(it.writeChannelState()) doReturn channelMutableState
    }

    @BeforeEach
    fun setUp() {
        channelMutableState.setEndOfNewerMessages(true)

        whenever(attachmentUrlValidator.updateValidAttachmentsUrl(any(), any())) doAnswer { invocation ->
            invocation.arguments[0] as List<Message>
        }

        channelLogic = ChannelLogic(
            repos,
            false,
            channelStateLogic,
            testCoroutines.scope,
        ) { CURRENT_USER_ID }
    }

    // User watching event
    @Test
    fun `when user watching event arrives, last message should upsert messages, increment count and appear`() = runTest {
        val user = User()
        val newDate = Date(Long.MAX_VALUE)

        val newMessage = randomMessage(
            id = "thisId",
            createdAt = newDate,
            silent = false,
            showInChannel = true,
        )

        whenever(attachmentUrlValidator.updateValidAttachmentsUrl(any(), any())) doReturn listOf(newMessage)

        val userStartWatchingEvent = randomNewMessageEvent(user = user, createdAt = newDate, message = newMessage)

        channelLogic.handleEvent(userStartWatchingEvent)

        verify(channelStateLogic).upsertMessage(newMessage)
        verify(channelStateLogic).updateCurrentUserRead(userStartWatchingEvent.createdAt, userStartWatchingEvent.message)
        verify(channelStateLogic).toggleHidden(false)
    }

    // Message update
    @Test
    fun `when a message update for an existing message arrives, it is added`() = runTest {
        val messageId = randomString()
        val message = randomMessage(
            id = messageId,
            user = User(id = "otherUserId"),
            silent = false,
            showInChannel = true,
        )
        channelLogic.upsertMessages(listOf(message))

        val messageUpdateEvent = randomMessageUpdateEvent(message = message)

        channelLogic.handleEvent(messageUpdateEvent)

        verify(channelStateLogic).upsertMessages(listOf(messageUpdateEvent.message))
    }

    // Member added event
    @Test
    fun `when member is added, it should be propagated`(): Unit = runTest {
        val user = randomUser()
        val member = randomMember(user = user)
        val memberAddedEvent = randomMemberAddedEvent(user = user, member = member)

        channelLogic.handleEvent(memberAddedEvent)

        verify(channelStateLogic).addMember(member)
    }

    // Typing events
    @Test
    fun `when events of start and stop tying arrive, it should be correctly propagated`() = runTest {
        val user1 = randomUser()
        val user2 = randomUser()

        val typingStartEvent1 = randomTypingStartEvent(user = user1, channelId = channelId)
        val typingStopEvent = randomTypingStopEvent(user = user2, channelId = channelId)

        channelLogic.run {
            handleEvent(typingStartEvent1)
            verify(channelStateLogic).setTyping(user1.id, typingStartEvent1)

            handleEvent(typingStopEvent)
            verify(channelStateLogic).setTyping(user2.id, null)
        }
    }

    // Read event
    @Test
    fun `when read notification event arrives, it should be correctly propagated`() = runTest {
        val readEvent = randomNotificationMarkReadEvent(user = currentUser)

        channelLogic.handleEvent(readEvent)

        verify(channelStateLogic).updateRead(readEvent.toChannelUserRead())
    }

    // Read event notification
    @Test
    fun `when read event arrives, it should be correctly propagated`() = runTest {
        val readEvent = randomMessageReadEvent(user = currentUser)

        channelLogic.handleEvent(readEvent)

        verify(channelStateLogic).updateRead(readEvent.toChannelUserRead())
    }

    // Reaction event
    @Test
    fun `when reaction event arrives, if message is in the list, the message of the event should be upsert`(): Unit = runTest {
        val message = randomMessage(
            showInChannel = true,
            silent = false,
        )
        channelStateLogic.upsertMessages(listOf(message))
        val reactionEvent = randomReactionNewEvent(user = randomUser(), message = message)
        whenever(attachmentUrlValidator.updateValidAttachmentsUrl(any(), any())) doReturn listOf(message)

        channelLogic.handleEvent(reactionEvent)

        // Message is propagated
        verify(channelStateLogic).upsertMessages(listOf(reactionEvent.message))
    }

    // Channel deleted event
    @Test
    fun `when channel is deleted, messages are deleted too`() = runTest {
        val deleteChannelEvent = randomChannelDeletedEvent()

        channelLogic.handleEvent(deleteChannelEvent)

        verify(channelStateLogic).removeMessagesBefore(deleteChannelEvent.createdAt)
        verify(channelStateLogic).deleteChannel(deleteChannelEvent.createdAt)
    }

    // Poll deleted event
    @Test
    fun `when poll is deleted, it is removed from the state`() = runTest {
        val poll = randomPoll()
        val pollDeletedEvent = randomPollDeletedEvent(poll = poll)

        channelLogic.handleEvent(pollDeletedEvent)

        verify(channelStateLogic).deletePoll(poll)
    }

    private companion object {
        private const val CURRENT_USER_ID = "currentUserId"
    }
}
