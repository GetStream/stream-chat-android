/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.common.feature.messages.composer

import app.cash.turbine.test
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.state.GlobalState
import io.getstream.chat.android.client.audio.AudioPlayer
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.App
import io.getstream.chat.android.models.AppSettings
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.FileUploadConfig
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageModerationAction
import io.getstream.chat.android.models.MessageModerationDetails
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.Mention
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.MentionType
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.MessageInput
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.sdk.chat.audio.recording.StreamMediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File
import java.util.Date

internal class MessageComposerControllerTest {

    @Test
    fun `test valid URLs with LinkPattern`() {
        val pattern = MessageComposerController.LinkPattern
        val validUrls = listOf(
            "https://www.example.com",
            "http://www.example.com",
            "www.example.com",
            "example.com",
            "https://subdomain.example.com",
            "http://example.com/path/to/page?name=parameter&another=value",
            "example.co.uk",
        )
        validUrls.forEach { url ->
            assertTrue(pattern.matches(url), "Expected $url to be a valid URL")
        }
    }

    @Test
    fun `test invalid URLs with LinkPattern`() {
        val pattern = MessageComposerController.LinkPattern
        val invalidUrls = listOf(
            "http//www.example.com",
            "htp://example.com",
            "://example.com",
            "example",
            "http://example..com",
            "http://-example.com",
            "http://example",
        )
        invalidUrls.forEach { url ->
            assertFalse(pattern.matches(url), "Expected $url to be an invalid URL")
        }
    }

    @Test
    fun `test hasCommands is read from channelConfig`() = runTest {
        // given
        val commands = listOf(Command("giphy", "Add GIF", "giphy", set = "giphy"))
        val config = Config(commands = commands)
        val configFlow = MutableStateFlow(config)
        // when
        val controller = Fixture()
            .givenAppSettings(mock())
            .givenAudioPlayer(mock())
            .givenClientState(User("uid1"))
            .givenGlobalState()
            .givenChannelState(configState = configFlow)
            .get()
        // then
        assertTrue(controller.state.value.hasCommands)
    }

    @Test
    fun `test pollsEnabled is read from channelConfig`() = runTest {
        // given
        val config = Config(pollsEnabled = true)
        val configFlow = MutableStateFlow(config)
        // when
        val controller = Fixture()
            .givenAppSettings(mock())
            .givenAudioPlayer(mock())
            .givenClientState(User("uid1"))
            .givenGlobalState()
            .givenChannelState(configState = configFlow)
            .get()
        // then
        assertTrue(controller.state.value.pollsEnabled)
    }

    @Test
    fun `Given user mention When selectMention called Then message input is autocompleted with user name`() = runTest {
        // Given
        val controller = Fixture()
            .givenAppSettings(mock())
            .givenAudioPlayer(mock())
            .givenClientState(User("uid1"))
            .givenGlobalState()
            .givenChannelState()
            .get()
        val user = User(id = "user1", name = "John Doe")
        controller.setMessageInput("Hello @")

        // When
        controller.selectMention(user)

        // Then
        assertEquals("Hello @John Doe ", controller.messageInput.value.text)
        assertEquals(1, controller.state.value.selectedMentions.size)
        assertTrue(controller.state.value.selectedMentions.contains(Mention.User(user)))
    }

    @Test
    fun `Given partial mention text When selectMention called Then partial text is replaced with full name`() = runTest {
        // Given
        val controller = Fixture()
            .givenAppSettings(mock())
            .givenAudioPlayer(mock())
            .givenClientState(User("uid1"))
            .givenGlobalState()
            .givenChannelState()
            .get()
        val user = User(id = "user1", name = "John Doe")
        controller.setMessageInput("Hello @jo")

        // When
        controller.selectMention(user)

        // Then
        assertEquals("Hello @John Doe ", controller.messageInput.value.text)
    }

    @Test
    fun `Given custom mention When selectMention called Then message input is autocompleted with display text`() = runTest {
        // Given
        val controller = Fixture()
            .givenAppSettings(mock())
            .givenAudioPlayer(mock())
            .givenClientState(User("uid1"))
            .givenGlobalState()
            .givenChannelState()
            .get()
        val customMention = CustomTestMention("Channel Name")
        controller.setMessageInput("Notify @")

        // When
        controller.selectMention(customMention)

        // Then
        assertEquals("Notify @Channel Name ", controller.messageInput.value.text)
        assertEquals(1, controller.state.value.selectedMentions.size)
        assertTrue(controller.state.value.selectedMentions.contains(customMention))
    }

    @Test
    fun `Given multiple mentions When selectMention called multiple times Then all mentions are tracked`() = runTest {
        // Given
        val controller = Fixture()
            .givenAppSettings(mock())
            .givenAudioPlayer(mock())
            .givenClientState(User("uid1"))
            .givenGlobalState()
            .givenChannelState()
            .get()
        val user1 = User(id = "user1", name = "John Doe")
        val user2 = User(id = "user2", name = "Jane Smith")

        // When
        controller.setMessageInput("Hello @")
        controller.selectMention(user1)
        controller.setMessageInput("Hello @John Doe and @")
        controller.selectMention(user2)

        // Then
        assertEquals("Hello @John Doe and @Jane Smith ", controller.messageInput.value.text)
        assertEquals(2, controller.state.value.selectedMentions.size)
    }

    @Test
    fun `Given message input source is MentionSelected When selectMention called Then source is set to MentionSelected`() = runTest {
        // Given
        val controller = Fixture()
            .givenAppSettings(mock())
            .givenAudioPlayer(mock())
            .givenClientState(User("uid1"))
            .givenGlobalState()
            .givenChannelState()
            .get()
        val user = User(id = "user1", name = "John Doe")
        controller.setMessageInput("Hello @")

        // When
        controller.selectMention(user)

        // Then
        assertEquals(MessageInput.Source.MentionSelected, controller.messageInput.value.source)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given idle state When startRecording is called Then delegates to media recorder`() = runTest {
        // Given
        val mockFile: File = mock()
        val fixture = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(User("uid1"))
            .givenGlobalState()
            .givenChannelState()
            .givenMediaRecorderStartSuccess(mockFile)
        val controller = fixture.get()

        // When
        controller.startRecording()
        advanceUntilIdle()

        // Then
        verify(fixture.mediaRecorder).startAudioRecording(
            any<String>(),
            any<Long>(),
            any<Boolean>(),
        )
    }

    @Test
    fun `Given no attachments When updateSelectedAttachments called Then attachments are set`() = runTest {
        // Given
        val controller = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState()
            .get()
        val attachments = listOf(randomAttachment(), randomAttachment())

        // When
        controller.updateSelectedAttachments(attachments)

        // Then
        assertEquals(attachments, controller.state.value.attachments)
    }

    @Test
    fun `Given a command When selectCommand called Then inputFocusEvents emits`() = runTest {
        // Given
        val command = Command("giphy", "Search GIFs", "[text]", "fun_set")
        val controller = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState(
                configState = MutableStateFlow(Config(commands = listOf(command))),
            )
            .get()

        controller.inputFocusEvents.test {
            // When
            controller.selectCommand(command)

            // Then
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // region sendMessage tests

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given normal mode When sendMessage called Then chatClient sendMessage is invoked with correct message`() = runTest {
        // Given
        val currentUser = User("uid1")
        val sentMessage = randomMessage(cid = CID, text = "Hello World")
        val fixture = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(currentUser)
            .givenGlobalState()
            .givenChannelState()
            .givenSendMessage(sentMessage)
        val controller = fixture.get()

        val message = Message(cid = CID, text = "Hello World")
        val callback: Call.Callback<Message> = mock()

        // When
        controller.sendMessage(message, callback)
        advanceUntilIdle()

        // Then
        val messageCaptor = argumentCaptor<Message>()
        verify(fixture.chatClient).sendMessage(
            eq(CHANNEL_TYPE),
            eq(CHANNEL_ID),
            messageCaptor.capture(),
            eq(false),
        )
        val capturedMessage = messageCaptor.firstValue
        assertEquals("Hello World", capturedMessage.text)
        assertEquals(CID, capturedMessage.cid)
        assertFalse(capturedMessage.showInChannel)
        assertTrue(capturedMessage.skipEnrichUrl)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given normal mode When sendMessage called Then data is cleared`() = runTest {
        // Given
        val currentUser = User("uid1")
        val sentMessage = randomMessage(cid = CID, text = "Hello World")
        val fixture = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(currentUser)
            .givenGlobalState()
            .givenChannelState()
            .givenSendMessage(sentMessage)
        val controller = fixture.get()

        controller.setMessageInput("Hello World")
        controller.addSelectedAttachments(listOf(randomAttachment()))

        val message = Message(cid = CID, text = "Hello World")
        val callback: Call.Callback<Message> = mock()

        // When
        controller.sendMessage(message, callback)
        advanceUntilIdle()

        // Then
        assertEquals("", controller.messageInput.value.text)
        assertEquals(emptyList<Any>(), controller.state.value.attachments)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given thread mode with alsoSendToChannel true When sendMessage called Then showInChannel is true`() = runTest {
        // Given
        val currentUser = User("uid1")
        val parentMessage = randomMessage(id = "parent-id", cid = CID)
        val sentMessage = randomMessage(cid = CID, text = "Thread reply")
        val fixture = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(currentUser)
            .givenGlobalState()
            .givenChannelState()
            .givenSendMessage(sentMessage)
        val controller = fixture.get()

        controller.setMessageMode(MessageMode.MessageThread(parentMessage))
        controller.setAlsoSendToChannel(true)

        val message = Message(cid = CID, text = "Thread reply", parentId = "parent-id")
        val callback: Call.Callback<Message> = mock()

        // When
        controller.sendMessage(message, callback)
        advanceUntilIdle()

        // Then
        val messageCaptor = argumentCaptor<Message>()
        verify(fixture.chatClient).sendMessage(
            eq(CHANNEL_TYPE),
            eq(CHANNEL_ID),
            messageCaptor.capture(),
            eq(false),
        )
        val capturedMessage = messageCaptor.firstValue
        assertTrue(capturedMessage.showInChannel)
        assertEquals("Thread reply", capturedMessage.text)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given thread mode with alsoSendToChannel false When sendMessage called Then showInChannel is false`() = runTest {
        // Given
        val currentUser = User("uid1")
        val parentMessage = randomMessage(id = "parent-id", cid = CID)
        val sentMessage = randomMessage(cid = CID, text = "Thread reply")
        val fixture = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(currentUser)
            .givenGlobalState()
            .givenChannelState()
            .givenSendMessage(sentMessage)
        val controller = fixture.get()

        controller.setMessageMode(MessageMode.MessageThread(parentMessage))
        controller.setAlsoSendToChannel(false)

        val message = Message(cid = CID, text = "Thread reply", parentId = "parent-id")
        val callback: Call.Callback<Message> = mock()

        // When
        controller.sendMessage(message, callback)
        advanceUntilIdle()

        // Then
        val messageCaptor = argumentCaptor<Message>()
        verify(fixture.chatClient).sendMessage(
            eq(CHANNEL_TYPE),
            eq(CHANNEL_ID),
            messageCaptor.capture(),
            eq(false),
        )
        val capturedMessage = messageCaptor.firstValue
        assertFalse(capturedMessage.showInChannel)
        assertEquals("Thread reply", capturedMessage.text)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given edit mode with text changes When sendMessage called Then editMessage is invoked`() = runTest {
        // Given
        val currentUser = User("uid1")
        val originalMessage = randomMessage(
            id = "msg-id",
            cid = CID,
            text = "Original text",
            user = currentUser,
        )
        val updatedMessage = originalMessage.copy(text = "Updated text")
        val fixture = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(currentUser)
            .givenGlobalState()
            .givenChannelState()
            .givenEditMessage(updatedMessage)
        val controller = fixture.get()

        // Enter edit mode
        controller.performMessageAction(Edit(originalMessage))

        val message = originalMessage.copy(text = "Updated text")
        val callback: Call.Callback<Message> = mock()

        // When
        controller.sendMessage(message, callback)
        advanceUntilIdle()

        // Then
        verify(fixture.chatClient).editMessage(
            eq("messaging"),
            eq("123"),
            any(),
        )
        verify(fixture.chatClient, never()).sendMessage(any(), any(), any(), any())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given edit mode with no text changes When sendMessage called Then no API call is made and data is cleared`() = runTest {
        // Given
        val currentUser = User("uid1")
        val originalMessage = randomMessage(
            id = "msg-id",
            cid = CID,
            text = "Same text",
            user = currentUser,
        )
        val fixture = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(currentUser)
            .givenGlobalState()
            .givenChannelState()
        val controller = fixture.get()

        // Enter edit mode
        controller.performMessageAction(Edit(originalMessage))

        // Send message with same text
        val message = originalMessage.copy(text = "Same text")
        val callback: Call.Callback<Message> = mock()

        // When
        controller.sendMessage(message, callback)
        advanceUntilIdle()

        // Then - no API calls should be made
        verify(fixture.chatClient, never()).editMessage(any(), any(), any())
        verify(fixture.chatClient, never()).sendMessage(any(), any(), any(), any())
        // And data should be cleared
        assertEquals("", controller.messageInput.value.text)
        assertEquals(emptyList<Any>(), controller.state.value.attachments)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given moderation error message When sendMessage called Then old message is deleted and new one is sent`() = runTest {
        // Given
        val currentUser = User("uid1")
        val moderationErrorMessage = randomMessage(
            id = "moderated-msg-id",
            cid = CID,
            text = "Bad content",
            user = currentUser,
            type = MessageType.ERROR,
            syncStatus = SyncStatus.FAILED_PERMANENTLY,
        ).copy(
            moderationDetails = MessageModerationDetails(
                originalText = "Bad content",
                action = MessageModerationAction.bounce,
                errorMsg = "Content moderation error",
            ),
        )
        val newMessage = Message(cid = CID, text = "Fixed content")
        val sentMessage = randomMessage(cid = CID, text = "Fixed content")
        val fixture = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(currentUser)
            .givenCurrentUser(currentUser)
            .givenGlobalState()
            .givenChannelState()
            .givenDeleteMessage(moderationErrorMessage)
            .givenSendMessage(sentMessage)
        val controller = fixture.get()

        // Enter edit mode with moderation error message
        controller.performMessageAction(Edit(moderationErrorMessage))

        val callback: Call.Callback<Message> = mock()

        // When
        controller.sendMessage(newMessage, callback)
        advanceUntilIdle()

        // Then
        verify(fixture.chatClient).deleteMessage(eq("moderated-msg-id"), eq(true))
        val messageCaptor = argumentCaptor<Message>()
        verify(fixture.chatClient).sendMessage(
            eq(CHANNEL_TYPE),
            eq(CHANNEL_ID),
            messageCaptor.capture(),
            eq(false),
        )
        assertEquals("Fixed content", messageCaptor.firstValue.text)
    }

    // endregion

    // region loadNewestMessages tests
    //
    // Why we verify `inheritScope` instead of `queryChannel`:
    //
    // The `loadNewestMessages` extension function internally uses `logic.channel().watch()` which
    // relies on the StatePlugin's LogicRegistry - a complex internal dependency that cannot be
    // easily mocked in unit tests. However, `loadNewestMessages` creates a `CoroutineCall` using
    // `chatClient.inheritScope()` as its very first operation:
    //
    //   fun ChatClient.loadNewestMessages(...): Call<Channel> {
    //       return CoroutineCall(inheritScope { Job(it) }) { ... }
    //   }
    //
    // Therefore, verifying whether `inheritScope` was called serves as a reliable proxy to confirm
    // whether `loadNewestMessages` was invoked, without needing to mock the entire StatePlugin
    // infrastructure.
    //

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given endOfNewerMessages is true When sendMessage called Then loadNewestMessages is not called`() = runTest {
        // Given
        val currentUser = User("uid1")
        val sentMessage = randomMessage(cid = CID, text = "Hello")
        val fixture = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(currentUser)
            .givenGlobalState()
            .givenChannelState(endOfNewerMessagesState = MutableStateFlow(true))
            .givenSendMessage(sentMessage)
        val controller = fixture.get()

        val message = Message(cid = CID, text = "Hello")
        val callback: Call.Callback<Message> = mock()

        // When
        controller.sendMessage(message, callback)
        advanceUntilIdle()

        // Then
        verify(fixture.chatClient, never()).inheritScope(any())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given thread mode and endOfNewerMessages is false When sendMessage called Then loadNewestMessages is not called`() = runTest {
        // Given
        val currentUser = User("uid1")
        val parentMessage = randomMessage(id = "parent-id", cid = CID)
        val sentMessage = randomMessage(cid = CID, text = "Thread reply")
        val fixture = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(currentUser)
            .givenGlobalState()
            .givenChannelState(endOfNewerMessagesState = MutableStateFlow(false))
            .givenSendMessage(sentMessage)
        val controller = fixture.get()

        controller.setMessageMode(MessageMode.MessageThread(parentMessage))

        val message = Message(cid = CID, text = "Thread reply", parentId = "parent-id")
        val callback: Call.Callback<Message> = mock()

        // When
        controller.sendMessage(message, callback)
        advanceUntilIdle()

        // Then
        verify(fixture.chatClient, never()).inheritScope(any())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given endOfNewerMessages is false When sendMessage called Then loadNewestMessages is called`() = runTest {
        // Given
        val currentUser = User("uid1")
        val sentMessage = randomMessage(cid = CID, text = "Hello")
        val fixture = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(currentUser)
            .givenGlobalState()
            .givenChannelState(endOfNewerMessagesState = MutableStateFlow(false))
            .givenSendMessage(sentMessage)
        val controller = fixture.get()

        val message = Message(cid = CID, text = "Hello")
        val callback: Call.Callback<Message> = mock()

        // When
        controller.sendMessage(message, callback)
        advanceUntilIdle()

        // Then
        verify(fixture.chatClient).inheritScope(any())
    }

    // endregion

    /**
     * Custom test implementation of [Mention] for testing purposes.
     */
    private data class CustomTestMention(
        override val display: String,
    ) : Mention {
        override val type: MentionType = MentionType("channel")
    }

    private class Fixture(
        val chatClient: ChatClient = mock(),
        private val cid: String = CID,
    ) {

        private val clientState: ClientState = mock()
        private val channelState: ChannelState = mock()
        private val globalState: GlobalState = mock()
        private val inheritedScope: CoroutineScope = TestScope()
        val mediaRecorder: StreamMediaRecorder = mock()

        fun givenAppSettings(appSettings: AppSettings = defaultAppSettings()) = apply {
            whenever(chatClient.getAppSettings()) doReturn appSettings
        }

        private fun defaultAppSettings(): AppSettings {
            val fileUploadConfig = FileUploadConfig(
                allowedFileExtensions = emptyList(),
                allowedMimeTypes = emptyList(),
                blockedFileExtensions = emptyList(),
                blockedMimeTypes = emptyList(),
                sizeLimitInBytes = AppSettings.DEFAULT_SIZE_LIMIT_IN_BYTES,
            )
            return AppSettings(
                app = App(
                    name = "test-app",
                    fileUploadConfig = fileUploadConfig,
                    imageUploadConfig = fileUploadConfig,
                ),
            )
        }

        fun givenAudioPlayer(audioPlayer: AudioPlayer) = apply {
            whenever(chatClient.audioPlayer) doReturn audioPlayer
        }

        fun givenMediaRecorderStartSuccess(file: File) = apply {
            whenever(
                mediaRecorder.startAudioRecording(any<String>(), any<Long>(), any<Boolean>()),
            ) doReturn Result.Success(file)
        }

        fun givenClientState(user: User) = apply {
            whenever(clientState.user) doReturn MutableStateFlow(user)
            whenever(chatClient.clientState) doReturn clientState
        }

        fun givenCurrentUser(user: User) = apply {
            whenever(chatClient.getCurrentUser()) doReturn user
        }

        fun givenChannelState(
            channelDataState: StateFlow<ChannelData> = MutableStateFlow(
                value = ChannelData(CHANNEL_ID, CHANNEL_TYPE),
            ),
            configState: StateFlow<Config> = MutableStateFlow(Config()),
            membersState: StateFlow<List<Member>> = MutableStateFlow(emptyList()),
            lastSentMessageDateState: StateFlow<Date?> = MutableStateFlow(null),
            endOfNewerMessagesState: StateFlow<Boolean> = MutableStateFlow(true),
        ) = apply {
            whenever(channelState.cid) doReturn cid
            whenever(channelState.channelData) doReturn channelDataState
            whenever(channelState.channelConfig) doReturn configState
            whenever(channelState.members) doReturn membersState
            whenever(channelState.lastSentMessageDate) doReturn lastSentMessageDateState
            whenever(channelState.endOfNewerMessages) doReturn endOfNewerMessagesState
        }

        fun givenSendMessage(message: Message) = apply {
            whenever(chatClient.sendMessage(any(), any(), any(), any())) doReturn message.asCall()
            whenever(chatClient.markMessageRead(any(), any(), any())) doReturn Unit.asCall()
        }

        fun givenEditMessage(message: Message) = apply {
            whenever(chatClient.editMessage(any(), any(), any())) doReturn message.asCall()
        }

        fun givenDeleteMessage(message: Message) = apply {
            whenever(chatClient.deleteMessage(any(), any())) doReturn message.asCall()
        }

        fun givenGlobalState(
            channelDrafts: Map<String, DraftMessage> = mapOf(),
            threadDrafts: Map<String, DraftMessage> = mapOf(),
        ) = apply {
            whenever(globalState.channelDraftMessages) doReturn MutableStateFlow(channelDrafts)
            whenever(globalState.threadDraftMessages) doReturn MutableStateFlow(threadDrafts)
        }

        fun get(): MessageComposerController {
            whenever(chatClient.inheritScope(any())) doReturn inheritedScope

            return MessageComposerController(
                channelCid = cid,
                chatClient = chatClient,
                channelState = MutableStateFlow(channelState),
                mediaRecorder = mediaRecorder,
                userLookupHandler = mock(),
                fileToUri = mock(),
                globalState = MutableStateFlow(globalState),
            )
        }
    }

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        private const val CHANNEL_TYPE = "messaging"
        private const val CHANNEL_ID = "123"
        private const val CID = "messaging:123"
    }
}
