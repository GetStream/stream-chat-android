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

import androidx.lifecycle.SavedStateHandle
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
import io.getstream.chat.android.randomCommand
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.MockRetrofitCall
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.Mention
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.MentionType
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.UserLookupHandler
import io.getstream.chat.android.ui.common.helper.internal.AttachmentStorageHelper.Companion.EXTRA_SOURCE_URI
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.MessageInput
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.Reply
import io.getstream.chat.android.ui.common.state.messages.ThreadReply
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerViewEvent
import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.sdk.chat.audio.recording.StreamMediaRecorder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File
import java.util.Date

@Suppress("LargeClass")
@OptIn(ExperimentalCoroutinesApi::class)
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
    fun `Given no attachments When addAttachments called Then attachments are set`() = runTest {
        // Given
        val controller = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState()
            .get()
        val attachments = listOf(
            randomAttachment(extraData = mapOf("io.getstream.sourceUri" to "uri:1")),
            randomAttachment(extraData = mapOf("io.getstream.sourceUri" to "uri:2")),
        )

        // When
        controller.addAttachments(attachments)

        // Then
        assertEquals(attachments.size, controller.state.value.attachments.size)
    }

    @Test
    fun `Given staged attachments When removeAttachment called Then the matching URI is removed`() = runTest {
        // Given
        val controller = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState()
            .get()
        val a1 = randomAttachment(extraData = mapOf(EXTRA_SOURCE_URI to "uri:1"))
        val a2 = randomAttachment(extraData = mapOf(EXTRA_SOURCE_URI to "uri:2"))
        controller.addAttachments(listOf(a1, a2))

        // When
        controller.removeAttachment(a1)

        // Then
        assertEquals(1, controller.state.value.attachments.size)
        assertEquals("uri:2", controller.state.value.attachments.first().extraData[EXTRA_SOURCE_URI])
    }

    @Test
    fun `Given staged attachments When removeAttachment called with unknown URI Then nothing changes`() = runTest {
        // Given
        val controller = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState()
            .get()
        val a1 = randomAttachment(extraData = mapOf(EXTRA_SOURCE_URI to "uri:1"))
        controller.addAttachments(listOf(a1))
        val unknown = randomAttachment(extraData = mapOf(EXTRA_SOURCE_URI to "uri:unknown"))

        // When
        controller.removeAttachment(unknown)

        // Then
        assertEquals(1, controller.state.value.attachments.size)
    }

    @Test
    fun `Given staged attachments When removeAttachmentsByUris called Then matching URIs are removed`() = runTest {
        // Given
        val controller = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState()
            .get()
        val a1 = randomAttachment(extraData = mapOf(EXTRA_SOURCE_URI to "uri:1"))
        val a2 = randomAttachment(extraData = mapOf(EXTRA_SOURCE_URI to "uri:2"))
        val a3 = randomAttachment(extraData = mapOf(EXTRA_SOURCE_URI to "uri:3"))
        controller.addAttachments(listOf(a1, a2, a3))

        // When
        controller.removeAttachmentsByUris(setOf("uri:1", "uri:3"))

        // Then
        assertEquals(1, controller.state.value.attachments.size)
        assertEquals("uri:2", controller.state.value.attachments.first().extraData[EXTRA_SOURCE_URI])
    }

    @Test
    fun `Given staged attachments When removeAttachmentsByUris called with empty set Then nothing changes`() = runTest {
        // Given
        val controller = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState()
            .get()
        val a1 = randomAttachment(extraData = mapOf(EXTRA_SOURCE_URI to "uri:1"))
        controller.addAttachments(listOf(a1))

        // When
        controller.removeAttachmentsByUris(emptySet())

        // Then
        assertEquals(1, controller.state.value.attachments.size)
    }

    @Test
    fun `Given staged attachments When clearAttachments called Then attachments list is empty`() = runTest {
        // Given
        val controller = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState()
            .get()
        controller.addAttachments(
            listOf(
                randomAttachment(extraData = mapOf(EXTRA_SOURCE_URI to "uri:1")),
                randomAttachment(extraData = mapOf(EXTRA_SOURCE_URI to "uri:2")),
            ),
        )

        // When
        controller.clearAttachments()

        // Then
        assertTrue(controller.state.value.attachments.isEmpty())
    }

    @Test
    fun `Given a staged attachment When addAttachments called with same URI Then value is updated and count stays the same`() = runTest {
        // Given
        val controller = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState()
            .get()
        val original = randomAttachment(extraData = mapOf(EXTRA_SOURCE_URI to "uri:1"))
        val updated = original.copy(type = "updated_type", extraData = mapOf(EXTRA_SOURCE_URI to "uri:1"))
        controller.addAttachments(listOf(original))

        // When
        controller.addAttachments(listOf(updated))

        // Then
        assertEquals(1, controller.state.value.attachments.size)
        assertEquals("updated_type", controller.state.value.attachments.first().type)
    }

    @Test
    fun `Given attachments added and cleared When state is collected Then it reflects the updated attachments`() = runTest {
        // Given
        val controller = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState()
            .get()
        val a1 = randomAttachment(extraData = mapOf(EXTRA_SOURCE_URI to "uri:1"))
        val a2 = randomAttachment(extraData = mapOf(EXTRA_SOURCE_URI to "uri:2"))

        controller.state.test {
            awaitItem() // initial empty emission

            // When
            controller.addAttachments(listOf(a1, a2))
            assertEquals(2, awaitItem().attachments.size)

            controller.clearAttachments()
            assertTrue(awaitItem().attachments.isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given picker attachments staged When entering and dismissing edit mode Then picker selections are preserved`() = runTest {
        // Given
        val controller = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState()
            .get()
        val pickerAttachment = randomAttachment(extraData = mapOf(EXTRA_SOURCE_URI to "uri:1"))
        controller.addAttachments(listOf(pickerAttachment))

        // When
        controller.performMessageAction(Edit(randomMessage(cid = CID)))
        controller.dismissMessageActions()

        // Then
        assertEquals(1, controller.state.value.attachments.size)
        assertEquals("uri:1", controller.state.value.attachments.first().extraData[EXTRA_SOURCE_URI])
    }

    @Test
    fun `Given edit mode with remote attachment When add picker attachment Then both are staged`() = runTest {
        // Given
        val remoteAttachment = randomAttachment()
        val pickerAttachment = randomAttachment(extraData = mapOf(EXTRA_SOURCE_URI to "uri:picker"))
        val controller = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState()
            .get()
        controller.performMessageAction(Edit(randomMessage(cid = CID, attachments = listOf(remoteAttachment))))

        // When
        controller.addAttachments(listOf(pickerAttachment))

        // Then
        assertEquals(2, controller.state.value.attachments.size)
    }

    @Test
    fun `Given edit mode with remote attachment When remove remote attachment Then it is removed`() = runTest {
        // Given
        val remoteAttachment = randomAttachment()
        val controller = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState()
            .get()
        controller.performMessageAction(Edit(randomMessage(cid = CID, attachments = listOf(remoteAttachment))))

        // When
        controller.removeAttachment(remoteAttachment)

        // Then
        assertTrue(controller.state.value.attachments.isEmpty())
    }

    @Test
    fun `Given edit mode When dismiss Then edit attachments are cleared and picker selections are preserved`() = runTest {
        // Given
        val remoteAttachment = randomAttachment()
        val pickerAttachment = randomAttachment(extraData = mapOf(EXTRA_SOURCE_URI to "uri:picker"))
        val controller = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState()
            .get()
        controller.addAttachments(listOf(pickerAttachment))
        controller.performMessageAction(Edit(randomMessage(cid = CID, attachments = listOf(remoteAttachment))))

        // When
        controller.dismissMessageActions()

        // Then
        assertEquals(1, controller.state.value.attachments.size)
        assertEquals("uri:picker", controller.state.value.attachments.first().extraData[EXTRA_SOURCE_URI])
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

    @Test
    fun `Given activeCommand is disabled When selectCommand called Then activeCommand is set and inputValue is not empty`() = runTest {
        // Given
        val command = Command("giphy", "Search GIFs", "[text]", "fun_set")
        val controller = Fixture()
            .givenConfig(MessageComposerController.Config(activeCommandEnabled = false))
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState(
                configState = MutableStateFlow(Config(commands = listOf(command))),
            )
            .get()

        // When
        controller.selectCommand(command)
        advanceUntilIdle()

        // Then
        assertEquals(command, controller.state.value.activeCommand)
        assertEquals("/${command.name} ", controller.state.value.inputValue)
    }

    @Test
    fun `Given a command When selectCommand called Then activeCommand is set and inputValue is empty`() = runTest {
        // Given
        val command = Command("giphy", "Search GIFs", "[text]", "fun_set")
        val controller = Fixture()
            .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState(
                configState = MutableStateFlow(Config(commands = listOf(command))),
            )
            .get()

        // When
        controller.selectCommand(command)
        advanceUntilIdle()

        // Then
        assertEquals(command, controller.state.value.activeCommand)
        assertEquals("", controller.state.value.inputValue)
    }

    @Test
    fun `Given an active command When clearActiveCommand called Then activeCommand is null and inputValue is empty`() =
        runTest {
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
            controller.selectCommand(command)
            advanceUntilIdle()

            // When
            controller.clearActiveCommand()
            advanceUntilIdle()

            // Then
            assertEquals(null, controller.state.value.activeCommand)
            assertEquals("", controller.state.value.inputValue)
        }

    @Test
    fun `Given pre-command input and attachments When selectCommand called Then composer is cleared`() = runTest {
        // Given
        val command = randomCommand()
        val preCommandText = "draft message"
        val preCommandAttachment = randomAttachment()
        val controller = Fixture()
            .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState(
                configState = MutableStateFlow(Config(commands = listOf(command))),
            )
            .get()
        controller.setMessageInput(preCommandText)
        controller.addAttachments(listOf(preCommandAttachment))
        advanceUntilIdle()

        // When
        controller.selectCommand(command)
        advanceUntilIdle()

        // Then
        assertEquals("", controller.state.value.inputValue)
        assertEquals(emptyList<Any>(), controller.state.value.attachments)
        assertEquals(command, controller.state.value.activeCommand)
    }

    @Test
    fun `Given pre-command input and attachments When clearActiveCommand called Then pre-command state is restored`() =
        runTest {
            // Given
            val command = randomCommand()
            val preCommandText = "draft message"
            val preCommandAttachment = randomAttachment()
            val controller = Fixture()
                .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState(
                    configState = MutableStateFlow(Config(commands = listOf(command))),
                )
                .get()
            controller.setMessageInput(preCommandText)
            controller.addAttachments(listOf(preCommandAttachment))
            advanceUntilIdle()
            controller.selectCommand(command)
            advanceUntilIdle()

            // When
            controller.clearActiveCommand()
            advanceUntilIdle()

            // Then
            assertNull(controller.state.value.activeCommand)
            assertEquals(preCommandText, controller.state.value.inputValue)
            assertEquals(listOf(preCommandAttachment), controller.state.value.attachments)
        }

    @Test
    fun `Given command trigger text When selectCommand then clearActiveCommand called Then trigger is not restored`() =
        runTest {
            // Given
            val command = randomCommand()
            val controller = Fixture()
                .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState(
                    configState = MutableStateFlow(Config(commands = listOf(command))),
                )
                .get()
            controller.setMessageInput("/gi")
            advanceUntilIdle()

            // When
            controller.selectCommand(command)
            advanceUntilIdle()
            controller.clearActiveCommand()
            advanceUntilIdle()

            // Then: trigger characters are popup noise, not draft content — restore empty
            assertEquals("", controller.state.value.inputValue)
        }

    @Test
    fun `Given pre-command mention When clearActiveCommand called Then mention selection is restored`() = runTest {
        // Given
        val command = randomCommand()
        val mentionedUser = User(id = "user1", name = "John Doe")
        val controller = Fixture()
            .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState(
                configState = MutableStateFlow(Config(commands = listOf(command))),
            )
            .get()
        controller.setMessageInput("Hello @")
        controller.selectMention(mentionedUser)
        advanceUntilIdle()
        controller.selectCommand(command)
        advanceUntilIdle()

        // When
        controller.clearActiveCommand()
        advanceUntilIdle()

        // Then
        assertEquals("Hello @John Doe ", controller.state.value.inputValue)
        assertTrue(controller.state.value.selectedMentions.contains(Mention.User(mentionedUser)))
    }

    @Test
    fun `Given in-command text When clearActiveCommand called Then in-command text is discarded`() = runTest {
        // Given
        val command = randomCommand()
        val preCommandText = "draft message"
        val inCommandText = "cat"
        val controller = Fixture()
            .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState(
                configState = MutableStateFlow(Config(commands = listOf(command))),
            )
            .get()
        controller.setMessageInput(preCommandText)
        advanceUntilIdle()
        controller.selectCommand(command)
        advanceUntilIdle()
        controller.setMessageInput(inCommandText)
        advanceUntilIdle()

        // When
        controller.clearActiveCommand()
        advanceUntilIdle()

        // Then
        assertEquals(preCommandText, controller.state.value.inputValue)
    }

    @Test
    fun `Given activeCommandEnabled disabled When selectCommand called Then pre-command input is not stashed`() =
        runTest {
            // Given
            val command = randomCommand()
            val preCommandText = "draft message"
            val controller = Fixture()
                .givenConfig(MessageComposerController.Config(activeCommandEnabled = false))
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState(
                    configState = MutableStateFlow(Config(commands = listOf(command))),
                )
                .get()
            controller.setMessageInput(preCommandText)
            advanceUntilIdle()
            controller.selectCommand(command)
            advanceUntilIdle()

            // When
            controller.clearActiveCommand()
            advanceUntilIdle()

            // Then (legacy behaviour: no stash, input cleared to empty on dismiss)
            assertEquals("", controller.state.value.inputValue)
        }

    @Test
    fun `Given re-selecting command When selectCommand called again Then existing stash is preserved`() = runTest {
        // Given
        val command = randomCommand()
        val otherCommand = randomCommand()
        val preCommandText = "draft message"
        val controller = Fixture()
            .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState(
                configState = MutableStateFlow(Config(commands = listOf(command, otherCommand))),
            )
            .get()
        controller.setMessageInput(preCommandText)
        advanceUntilIdle()
        controller.selectCommand(command)
        advanceUntilIdle()
        controller.setMessageInput("cat")
        advanceUntilIdle()

        // When: user re-selects a command without cancelling the first one
        controller.selectCommand(otherCommand)
        advanceUntilIdle()
        controller.clearActiveCommand()
        advanceUntilIdle()

        // Then: dismissal restores the original pre-command draft, not the in-command text
        assertEquals(preCommandText, controller.state.value.inputValue)
    }

    @Test
    fun `Given an active command with stash When clearData called Then stash is discarded and composer is empty`() =
        runTest {
            // Given
            val command = randomCommand()
            val preCommandText = "draft message"
            val preCommandAttachment = randomAttachment()
            val controller = Fixture()
                .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState(
                    configState = MutableStateFlow(Config(commands = listOf(command))),
                )
                .get()
            controller.setMessageInput(preCommandText)
            controller.addAttachments(listOf(preCommandAttachment))
            advanceUntilIdle()
            controller.selectCommand(command)
            advanceUntilIdle()

            // When
            controller.clearData()
            advanceUntilIdle()

            // Then
            assertNull(controller.state.value.activeCommand)
            assertEquals("", controller.state.value.inputValue)
            assertEquals(emptyList<Any>(), controller.state.value.attachments)
        }

    @Test
    fun `Given edit mode When selectCommand called Then activeCommand is not set and event is emitted`() = runTest {
        // Given
        val command = randomCommand()
        val editedMessage = randomMessage(cid = CID)
        val editAction = Edit(editedMessage)
        val controller = Fixture()
            .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState(
                configState = MutableStateFlow(Config(commands = listOf(command))),
            )
            .get()
        controller.performMessageAction(editAction)
        advanceUntilIdle()

        // When / Then
        controller.events.test {
            controller.selectCommand(command)
            advanceUntilIdle()

            assertEquals(MessageComposerViewEvent.CommandUnavailable(editAction), awaitItem())
            assertNull(controller.state.value.activeCommand)
            assertEquals(editedMessage.text, controller.state.value.inputValue)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given reply mode When selectCommand called with moderation command Then command is not set and event is emitted`() =
        runTest {
            // Given
            val muteCommand = randomCommand(set = MODERATION_SET)
            val repliedMessage = randomMessage(cid = CID)
            val replyAction = Reply(repliedMessage)
            val controller = Fixture()
                .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState(
                    configState = MutableStateFlow(Config(commands = listOf(muteCommand))),
                )
                .get()
            controller.performMessageAction(replyAction)
            advanceUntilIdle()

            // When / Then
            controller.events.test {
                controller.selectCommand(muteCommand)
                advanceUntilIdle()

                assertEquals(MessageComposerViewEvent.CommandUnavailable(replyAction), awaitItem())
                assertNull(controller.state.value.activeCommand)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `Given reply mode When selectCommand then clearActiveCommand called Then reply action is preserved`() = runTest {
        // Given
        val funCommand = randomCommand()
        val repliedMessage = randomMessage(cid = CID)
        val replyAction = Reply(repliedMessage)
        val controller = Fixture()
            .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState(
                configState = MutableStateFlow(Config(commands = listOf(funCommand))),
            )
            .get()
        controller.performMessageAction(replyAction)
        advanceUntilIdle()

        // When
        controller.selectCommand(funCommand)
        advanceUntilIdle()
        controller.clearActiveCommand()
        advanceUntilIdle()

        // Then
        assertEquals(replyAction, controller.state.value.action)
        assertNull(controller.state.value.activeCommand)
    }

    @Test
    fun `Given picker attachment When user types slash Then command suggestions include all commands`() = runTest {
        // Given
        val command = randomCommand()
        val pickerAttachment = randomAttachment(extraData = mapOf(EXTRA_SOURCE_URI to "uri:1"))
        val controller = Fixture()
            .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState(
                configState = MutableStateFlow(Config(commands = listOf(command))),
            )
            .get()
        controller.addAttachments(listOf(pickerAttachment))
        advanceUntilIdle()

        // When
        controller.setMessageInput("/")
        advanceUntilIdle()

        // Then
        assertEquals(listOf(command), controller.state.value.commandSuggestions)
    }

    @Test
    fun `Given edit mode When user types slash Then suggestions stay empty and event is emitted`() = runTest {
        // Given
        val command = randomCommand()
        val editedMessage = randomMessage(cid = CID, text = "")
        val editAction = Edit(editedMessage)
        val controller = Fixture()
            .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState(
                configState = MutableStateFlow(Config(commands = listOf(command))),
            )
            .get()
        controller.performMessageAction(editAction)
        advanceUntilIdle()

        // When / Then
        controller.events.test {
            controller.setMessageInput("/")
            advanceUntilIdle()

            assertEquals(MessageComposerViewEvent.CommandUnavailable(editAction), awaitItem())
            assertTrue(controller.state.value.commandSuggestions.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given edit mode When user types slash prefix that matches no command Then no event is emitted`() = runTest {
        // Given
        val command = randomCommand(name = "giphy")
        val editedMessage = randomMessage(cid = CID, text = "")
        val controller = Fixture()
            .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState(
                configState = MutableStateFlow(Config(commands = listOf(command))),
            )
            .get()
        controller.performMessageAction(Edit(editedMessage))
        advanceUntilIdle()

        // When / Then
        controller.events.test {
            controller.setMessageInput("/xyz")
            advanceUntilIdle()

            // Plain slash text, not a blocked command attempt → no snackbar.
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given slash typed Then suggestions are universal-first preserving backend order regardless of action`() = runTest {
        // Given the backend hands out commands in an order where a moderation command precedes a
        // universal one — the SDK's sort must move universal commands to the front while
        // preserving the backend's relative order within each tier.
        val banCommand = randomCommand(name = "ban", set = MODERATION_SET)
        val giphyCommand = randomCommand(name = "giphy", set = "")
        val muteCommand = randomCommand(name = "mute", set = MODERATION_SET)
        val unbanCommand = randomCommand(name = "unban", set = MODERATION_SET)
        val unmuteCommand = randomCommand(name = "unmute", set = MODERATION_SET)
        val repliedMessage = randomMessage(cid = CID)
        val controller = Fixture()
            .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState(
                configState = MutableStateFlow(
                    Config(
                        commands = listOf(banCommand, giphyCommand, muteCommand, unbanCommand, unmuteCommand),
                    ),
                ),
            )
            .get()
        val expectedOrder = listOf(giphyCommand, banCommand, muteCommand, unbanCommand, unmuteCommand)

        // When typing slash with the composer in its default state
        controller.setMessageInput("/")
        advanceUntilIdle()

        // Then suggestions surface universal commands first, with backend order preserved within each tier.
        assertEquals(expectedOrder, controller.state.value.commandSuggestions)

        // And switching to Reply mode preserves the same order — no re-shuffle.
        controller.performMessageAction(Reply(repliedMessage))
        advanceUntilIdle()
        assertEquals(expectedOrder, controller.state.value.commandSuggestions)
    }

    @Test
    fun `Given active moderation command When action becomes Reply Then event is emitted and action is not applied`() =
        runTest {
            // Given
            val muteCommand = randomCommand(set = MODERATION_SET)
            val controller = Fixture()
                .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState(
                    configState = MutableStateFlow(Config(commands = listOf(muteCommand))),
                )
                .get()
            controller.selectCommand(muteCommand)
            advanceUntilIdle()
            val replyAction = Reply(randomMessage(cid = CID))

            // When / Then
            controller.events.test {
                controller.performMessageAction(replyAction)
                advanceUntilIdle()

                assertEquals(MessageComposerViewEvent.CancelCommandRequired(replyAction), awaitItem())
                assertEquals(muteCommand, controller.state.value.activeCommand)
                assertNull(controller.state.value.action)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `Given active fun command When action becomes Reply Then action applies and activeCommand preserved`() =
        runTest {
            // Given
            val funCommand = randomCommand()
            val controller = Fixture()
                .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState(
                    configState = MutableStateFlow(Config(commands = listOf(funCommand))),
                )
                .get()
            controller.selectCommand(funCommand)
            advanceUntilIdle()
            val replyAction = Reply(randomMessage(cid = CID))

            // When
            controller.performMessageAction(replyAction)
            advanceUntilIdle()

            // Then
            assertEquals(replyAction, controller.state.value.action)
            assertEquals(funCommand, controller.state.value.activeCommand)
        }

    @Test
    fun `Given activeCommandEnabled true When user types slash name plus space Then command mode is entered`() = runTest {
        // Given
        val command = randomCommand(name = "giphy")
        val controller = Fixture()
            .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState(
                configState = MutableStateFlow(Config(commands = listOf(command))),
            )
            .get()

        // When
        controller.setMessageInput("/giphy ")
        advanceUntilIdle()

        // Then
        assertEquals(command, controller.state.value.activeCommand)
        assertEquals("", controller.state.value.inputValue)
    }

    @Test
    fun `Given auto-selected command via slash name space When clearActiveCommand Then chip is cleared and input is empty`() =
        runTest {
            // Given
            val command = randomCommand(name = "giphy")
            val controller = Fixture()
                .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState(
                    configState = MutableStateFlow(Config(commands = listOf(command))),
                )
                .get()
            controller.setMessageInput("/giphy ")
            advanceUntilIdle()
            assertEquals(command, controller.state.value.activeCommand)

            // When
            controller.clearActiveCommand()
            advanceUntilIdle()

            // Then
            assertNull(controller.state.value.activeCommand)
            assertEquals("", controller.state.value.inputValue)
        }

    @Test
    fun `Given auto-selected command When action changes Then auto-select does not re-fire`() = runTest {
        // Given
        val command = randomCommand(name = "giphy")
        val controller = Fixture()
            .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState(
                configState = MutableStateFlow(Config(commands = listOf(command))),
            )
            .get()
        controller.setMessageInput("/giphy ")
        advanceUntilIdle()
        assertEquals(command, controller.state.value.activeCommand)
        // Input is empty after auto-select, so action change should not re-trigger.

        // When
        controller.performMessageAction(Reply(randomMessage(cid = CID)))
        advanceUntilIdle()

        // Then
        assertEquals(command, controller.state.value.activeCommand)
        assertEquals("", controller.state.value.inputValue)
    }

    @Test
    fun `Given activeCommandEnabled false When user types slash name plus space Then command mode is not entered`() =
        runTest {
            // Given
            val command = randomCommand(name = "giphy")
            val controller = Fixture()
                .givenConfig(MessageComposerController.Config(activeCommandEnabled = false))
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState(
                    configState = MutableStateFlow(Config(commands = listOf(command))),
                )
                .get()

            // When
            controller.setMessageInput("/giphy ")
            advanceUntilIdle()

            // Then
            assertNull(controller.state.value.activeCommand)
            assertEquals("/giphy ", controller.state.value.inputValue)
        }

    @Test
    fun `Given activeCommandEnabled false and reply mode When selectCommand called with moderation command Then activeCommand is set`() =
        runTest {
            // Given
            val muteCommand = randomCommand(set = MODERATION_SET)
            val repliedMessage = randomMessage(cid = CID)
            val controller = Fixture()
                .givenConfig(MessageComposerController.Config(activeCommandEnabled = false))
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState(
                    configState = MutableStateFlow(Config(commands = listOf(muteCommand))),
                )
                .get()
            controller.performMessageAction(Reply(repliedMessage))
            advanceUntilIdle()

            // When
            controller.selectCommand(muteCommand)
            advanceUntilIdle()

            // Then
            assertEquals(muteCommand, controller.state.value.activeCommand)
        }

    @Test
    fun `Given activeCommandEnabled false and edit mode When user types slash Then command suggestions are shown`() =
        runTest {
            // Given
            val command = randomCommand()
            val editedMessage = randomMessage(cid = CID)
            val controller = Fixture()
                .givenConfig(MessageComposerController.Config(activeCommandEnabled = false))
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState(
                    configState = MutableStateFlow(Config(commands = listOf(command))),
                )
                .get()
            controller.performMessageAction(Edit(editedMessage))
            advanceUntilIdle()

            // When
            controller.setMessageInput("/")
            advanceUntilIdle()

            // Then (legacy: popup not suppressed in edit mode)
            assertEquals(listOf(command), controller.state.value.commandSuggestions)
        }

    @Test
    fun `Given activeCommandEnabled false and reply mode When toggleCommandsVisibility called Then commands keep server order`() =
        runTest {
            // Given
            val funCommand = randomCommand()
            val muteCommand = randomCommand(set = MODERATION_SET)
            val repliedMessage = randomMessage(cid = CID)
            val controller = Fixture()
                .givenConfig(MessageComposerController.Config(activeCommandEnabled = false))
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState(
                    configState = MutableStateFlow(Config(commands = listOf(muteCommand, funCommand))),
                )
                .get()
            controller.performMessageAction(Reply(repliedMessage))
            advanceUntilIdle()

            // When
            controller.toggleCommandsVisibility()
            advanceUntilIdle()

            // Then (legacy: server order preserved, moderation not pushed to the bottom)
            assertEquals(listOf(muteCommand, funCommand), controller.state.value.commandSuggestions)
        }

    @Test
    fun `Given slash moderation command typed in reply mode When sendMessage called Then event is emitted and message is not sent`() =
        runTest {
            // Given
            val muteCommand = randomCommand(name = "mute", set = MODERATION_SET)
            val replyAction = Reply(randomMessage(cid = CID))
            val controller = Fixture()
                .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState(
                    configState = MutableStateFlow(Config(commands = listOf(muteCommand))),
                )
                .get()
            controller.performMessageAction(replyAction)
            advanceUntilIdle()
            val message = Message(text = "/mute @alice")

            // When / Then
            controller.events.test {
                controller.sendMessage(message, callback = mock())
                advanceUntilIdle()

                assertEquals(MessageComposerViewEvent.CommandUnavailable(replyAction), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `Given active command When performMessageAction with ThreadReply Then no event is emitted and thread mode applies`() =
        runTest {
            // Given
            val muteCommand = randomCommand(set = MODERATION_SET)
            val parentMessage = randomMessage(cid = CID)
            val controller = Fixture()
                .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState(
                    configState = MutableStateFlow(Config(commands = listOf(muteCommand))),
                )
                .givenDraftMessageStubs()
                .get()
            controller.selectCommand(muteCommand)
            advanceUntilIdle()

            // When / Then — ThreadReply is compatible with the active command, so the conflict
            // guard must not emit CancelCommandRequired and the thread mode must apply.
            controller.events.test {
                controller.performMessageAction(ThreadReply(parentMessage))
                advanceUntilIdle()

                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(MessageMode.MessageThread(parentMessage), controller.state.value.messageMode)
        }

    @Test
    fun `Given reply mode When selectCommand called with fun_set command Then activeCommand is set`() = runTest {
        // Given
        val funCommand = randomCommand()
        val muteCommand = randomCommand(set = MODERATION_SET)
        val repliedMessage = randomMessage(cid = CID)
        val controller = Fixture()
            .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState(
                configState = MutableStateFlow(Config(commands = listOf(funCommand, muteCommand))),
            )
            .get()
        controller.performMessageAction(Reply(repliedMessage))
        advanceUntilIdle()

        // When
        controller.selectCommand(funCommand)
        advanceUntilIdle()

        // Then
        assertEquals(funCommand, controller.state.value.activeCommand)
    }

    @Test
    fun `Given reply mode When user types slash Then suggestions include all commands`() = runTest {
        // Given
        val funCommand = randomCommand()
        val muteCommand = randomCommand(set = MODERATION_SET)
        val repliedMessage = randomMessage(cid = CID)
        val controller = Fixture()
            .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState(
                configState = MutableStateFlow(Config(commands = listOf(funCommand, muteCommand))),
            )
            .get()
        controller.performMessageAction(Reply(repliedMessage))
        advanceUntilIdle()

        // When
        controller.setMessageInput("/")
        advanceUntilIdle()

        // Then
        assertEquals(listOf(funCommand, muteCommand), controller.state.value.commandSuggestions)
    }

    @Test
    fun `Given reply mode When toggleCommandsVisibility called Then suggestions include all commands`() = runTest {
        // Given
        val funCommand = randomCommand()
        val muteCommand = randomCommand(set = MODERATION_SET)
        val repliedMessage = randomMessage(cid = CID)
        val controller = Fixture()
            .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState(
                configState = MutableStateFlow(Config(commands = listOf(funCommand, muteCommand))),
            )
            .get()
        controller.performMessageAction(Reply(repliedMessage))
        advanceUntilIdle()

        // When
        controller.toggleCommandsVisibility()
        advanceUntilIdle()

        // Then
        assertEquals(listOf(funCommand, muteCommand), controller.state.value.commandSuggestions)
    }

    @Test
    fun `Given edit mode When toggleCommandsVisibility called Then command suggestions include all commands`() =
        runTest {
            // Given
            val command = randomCommand()
            val editedMessage = randomMessage(cid = CID)
            val controller = Fixture()
                .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState(
                    configState = MutableStateFlow(Config(commands = listOf(command))),
                )
                .get()
            controller.performMessageAction(Edit(editedMessage))
            advanceUntilIdle()

            // When
            controller.toggleCommandsVisibility()
            advanceUntilIdle()

            // Then
            assertEquals(listOf(command), controller.state.value.commandSuggestions)
        }

    @Test
    fun `Given an active command with args When buildNewMessage called Then full command text is used`() = runTest {
        // Given
        val command = randomCommand(name = "giphy")
        val controller = Fixture()
            .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState(
                configState = MutableStateFlow(Config(commands = listOf(command))),
            )
            .get()
        controller.selectCommand(command)
        advanceUntilIdle()

        // When
        val message = controller.buildNewMessage("hello world")

        // Then
        assertEquals("/giphy hello world", message.text)
    }

    @Test
    fun `Given an active command with no args When buildNewMessage called Then command name only is used`() = runTest {
        // Given
        val command = randomCommand(name = "giphy")
        val controller = Fixture()
            .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState(
                configState = MutableStateFlow(Config(commands = listOf(command))),
            )
            .get()
        controller.selectCommand(command)
        advanceUntilIdle()

        // When
        val message = controller.buildNewMessage("")

        // Then
        assertEquals("/giphy", message.text)
    }

    @Test
    fun `Given nameless mention When user id token present Then mention is kept`() = runTest {
        // Given
        val controller = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(User("uid1"))
            .givenGlobalState()
            .givenChannelState()
            .get()
        val user = User(id = "user1", name = "")
        controller.setMessageInput("Hello @")
        controller.selectMention(user)
        advanceUntilIdle()

        // When
        val message = controller.buildNewMessage(controller.messageInput.value.text)

        // Then
        assertEquals(listOf("user1"), message.mentionedUsersIds)
    }

    @Test
    fun `Given nameless mention When user id token removed Then mention is dropped`() = runTest {
        // Given
        val controller = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(User("uid1"))
            .givenGlobalState()
            .givenChannelState()
            .get()
        val user = User(id = "user1", name = "")
        controller.setMessageInput("Hello @")
        controller.selectMention(user)
        advanceUntilIdle()

        // When (user erases the mention text but a stray "@" remains)
        val message = controller.buildNewMessage("Hello @ world")

        // Then
        assertEquals(emptyList<String>(), message.mentionedUsersIds)
    }

    @Test
    fun `Given an active command When clearData called Then activeCommand is null`() = runTest {
        // Given
        val command = randomCommand()
        val controller = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState(
                configState = MutableStateFlow(Config(commands = listOf(command))),
            )
            .get()
        controller.selectCommand(command)
        advanceUntilIdle()

        // When
        controller.clearData()
        advanceUntilIdle()

        // Then
        assertNull(controller.state.value.activeCommand)
    }

    @Test
    fun `Given draft with command and args When controller initializes Then activeCommand is restored and args populate the input`() =
        runTest {
            // Given
            val giphyCommand = randomCommand(name = "giphy")
            val draft = DraftMessage(
                cid = CID,
                text = "",
                command = "giphy",
                args = "cat",
            )
            val controller = Fixture()
                .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState(channelDrafts = mapOf(CID to draft))
                .givenChannelState(
                    configState = MutableStateFlow(Config(commands = listOf(giphyCommand))),
                )
                .givenDraftMessageStubs()
                .get()
            advanceUntilIdle()

            // Then
            assertEquals(giphyCommand, controller.state.value.activeCommand)
            assertEquals("cat", controller.state.value.inputValue)
        }

    @Test
    fun `Given draft with unknown command When controller initializes Then command is not restored and text is preserved`() =
        runTest {
            // Given
            val draft = DraftMessage(
                cid = CID,
                text = "fallback",
                command = "ban",
                args = null,
            )
            val controller = Fixture()
                .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState(channelDrafts = mapOf(CID to draft))
                .givenChannelState(
                    configState = MutableStateFlow(Config(commands = listOf(randomCommand(name = "giphy")))),
                )
                .givenDraftMessageStubs()
                .get()
            advanceUntilIdle()

            // Then
            assertNull(controller.state.value.activeCommand)
            assertEquals("fallback", controller.state.value.inputValue)
        }

    @Test
    fun `Given draft with moderation command and reply When controller initializes Then command is silently skipped`() =
        runTest {
            // Given
            val muteCommand = randomCommand(name = "mute", set = MODERATION_SET)
            val repliedMessage = randomMessage(cid = CID)
            val draft = DraftMessage(
                cid = CID,
                text = "",
                command = "mute",
                args = "@alice",
                replyMessage = repliedMessage,
            )
            val controller = Fixture()
                .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState(channelDrafts = mapOf(CID to draft))
                .givenChannelState(
                    configState = MutableStateFlow(Config(commands = listOf(muteCommand))),
                )
                .givenDraftMessageStubs()
                .get()

            // When / Then — collect events, expect none for the draft restore
            controller.events.test {
                advanceUntilIdle()
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
            assertNull(controller.state.value.activeCommand)
            assertEquals(Reply(repliedMessage), controller.state.value.action)
        }

    @Test
    fun `Given activeCommandEnabled false When controller initializes with command draft Then command is ignored`() =
        runTest {
            // Given
            val draft = DraftMessage(
                cid = CID,
                text = "fallback text",
                command = "giphy",
                args = "cat",
            )
            val controller = Fixture()
                .givenConfig(MessageComposerController.Config(activeCommandEnabled = false))
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState(channelDrafts = mapOf(CID to draft))
                .givenChannelState(
                    configState = MutableStateFlow(Config(commands = listOf(randomCommand(name = "giphy")))),
                )
                .givenDraftMessageStubs()
                .get()
            advanceUntilIdle()

            // Then — legacy mode reads `text`, ignores command/args
            assertNull(controller.state.value.activeCommand)
            assertEquals("fallback text", controller.state.value.inputValue)
        }

    @Test
    fun `Given activeCommand and empty input When mode changes Then draft is saved with command and empty text`() =
        runTest {
            // Given
            val giphyCommand = randomCommand(name = "giphy")
            val parentMessage = randomMessage(cid = CID)
            val fixture = Fixture()
                .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState(
                    configState = MutableStateFlow(Config(commands = listOf(giphyCommand))),
                )
                .givenDraftMessageStubs()
            val controller = fixture.get()
            controller.selectCommand(giphyCommand)
            advanceUntilIdle()

            // When — mode change triggers saveDraftMessage on the previous (Normal) mode
            controller.setMessageMode(MessageMode.MessageThread(parentMessage))
            advanceUntilIdle()

            // Then — chatClient.createDraftMessage was called with command set and text empty
            val captor = argumentCaptor<DraftMessage>()
            verify(fixture.chatClient).createDraftMessage(eq(CHANNEL_TYPE), eq(CHANNEL_ID), captor.capture())
            val saved = captor.firstValue
            assertEquals("giphy", saved.command)
            assertEquals("", saved.text)
            assertEquals("", saved.args)
        }

    @Test
    fun `Given active command in normal mode When switching to thread Then leftover command does not block reply restore`() =
        runTest {
            // Given — Normal mode has a moderation command active. Thread draft has a reply pending.
            val muteCommand = randomCommand(name = "mute", set = MODERATION_SET)
            val parentMessage = randomMessage(cid = CID)
            val repliedInThread = randomMessage(cid = CID)
            val threadDraft = DraftMessage(
                cid = CID,
                parentId = parentMessage.id,
                text = "thread draft",
                replyMessage = repliedInThread,
            )
            val controller = Fixture()
                .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState(threadDrafts = mapOf(parentMessage.id to threadDraft))
                .givenChannelState(
                    configState = MutableStateFlow(Config(commands = listOf(muteCommand))),
                )
                .givenDraftMessageStubs()
                .get()
            controller.selectCommand(muteCommand)
            advanceUntilIdle()
            assertEquals(muteCommand, controller.state.value.activeCommand)

            // When
            controller.setMessageMode(MessageMode.MessageThread(parentMessage))
            advanceUntilIdle()

            // Then — the new mode's reply action applies; the leftover mute command is gone.
            assertNull(controller.state.value.activeCommand)
            assertEquals(Reply(repliedInThread), controller.state.value.action)
        }

    @Test
    fun `Given activeCommandEnabled false When draft is saved Then command and args are not persisted`() = runTest {
        // Given — legacy mode with a command active in state (legacy keeps slash inside text).
        val giphyCommand = randomCommand(name = "giphy")
        val parentMessage = randomMessage(cid = CID)
        val fixture = Fixture()
            .givenConfig(MessageComposerController.Config(activeCommandEnabled = false))
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState(
                configState = MutableStateFlow(Config(commands = listOf(giphyCommand))),
            )
            .givenDraftMessageStubs()
        val controller = fixture.get()
        controller.selectCommand(giphyCommand)
        controller.setMessageInput("/giphy cat")
        advanceUntilIdle()

        // When
        controller.setMessageMode(MessageMode.MessageThread(parentMessage))
        advanceUntilIdle()

        // Then — wire format stays clean: text holds the legacy form, command/args stay null.
        val captor = argumentCaptor<DraftMessage>()
        verify(fixture.chatClient).createDraftMessage(eq(CHANNEL_TYPE), eq(CHANNEL_ID), captor.capture())
        val saved = captor.firstValue
        assertEquals("/giphy cat", saved.text)
        assertNull(saved.command)
        assertNull(saved.args)
    }

    @Test
    fun `Given activeCommand and typed args When mode changes Then draft is saved with text and args populated`() =
        runTest {
            // Given
            val giphyCommand = randomCommand(name = "giphy")
            val parentMessage = randomMessage(cid = CID)
            val fixture = Fixture()
                .givenConfig(MessageComposerController.Config(activeCommandEnabled = true))
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState(
                    configState = MutableStateFlow(Config(commands = listOf(giphyCommand))),
                )
                .givenDraftMessageStubs()
            val controller = fixture.get()
            controller.selectCommand(giphyCommand)
            controller.setMessageInput("Test")
            advanceUntilIdle()

            // When
            controller.setMessageMode(MessageMode.MessageThread(parentMessage))
            advanceUntilIdle()

            // Then — text is populated (so channel-list previews render the user's input) and args
            // mirrors it for cross-SDK readers that distinguish them.
            val captor = argumentCaptor<DraftMessage>()
            verify(fixture.chatClient).createDraftMessage(eq(CHANNEL_TYPE), eq(CHANNEL_ID), captor.capture())
            val saved = captor.firstValue
            assertEquals("giphy", saved.command)
            assertEquals("Test", saved.text)
            assertEquals("Test", saved.args)
        }

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
        controller.addAttachments(listOf(randomAttachment()))

        val message = Message(cid = CID, text = "Hello World")
        val callback: Call.Callback<Message> = mock()

        // When
        controller.sendMessage(message, callback)
        advanceUntilIdle()

        // Then
        assertEquals("", controller.messageInput.value.text)
        assertEquals(emptyList<Any>(), controller.state.value.attachments)
    }

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

    @Test
    fun `Given thread mode with alsoSendToChannel true When clearData called Then alsoSendToChannel remains true`() =
        runTest {
            // Given
            val parentMessage = randomMessage()
            val fixture = Fixture()
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState()
            val controller = fixture.get()

            controller.setMessageMode(MessageMode.MessageThread(parentMessage))
            controller.setAlsoSendToChannel(true)

            // When
            controller.clearData()
            advanceUntilIdle()

            // Then
            assertTrue(controller.state.value.alsoSendToChannel)
        }

    @Test
    fun `Given normal mode with alsoSendToChannel true When clearData called Then alsoSendToChannel is false`() =
        runTest {
            // Given
            val fixture = Fixture()
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState()
            val controller = fixture.get()
            controller.setAlsoSendToChannel(true)

            // When
            controller.clearData()
            advanceUntilIdle()

            // Then
            assertFalse(controller.state.value.alsoSendToChannel)
        }

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

    @Test
    fun `Given URI-less attachments When addAttachments called Then all attachments are staged`() = runTest {
        val controller = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState()
            .get()
        val a1 = randomAttachment(name = "location.pin", type = "location")
        val a2 = randomAttachment(name = "card.custom", type = "custom")

        controller.addAttachments(listOf(a1, a2))

        assertEquals(2, controller.state.value.attachments.size)
    }

    @Test
    fun `Given URI-less attachment staged When removeAttachment called Then it is removed by fallback key`() = runTest {
        val controller = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState()
            .get()
        val attachment = randomAttachment(name = "location.pin", type = "location")
        controller.addAttachments(listOf(attachment))

        controller.removeAttachment(attachment)

        assertTrue(controller.state.value.attachments.isEmpty())
    }

    @Test
    fun `Given edit-mode attachment with URI When removeAttachment called Then edit-mode list is checked before picker`() =
        runTest {
            val controller = Fixture()
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState()
                .get()
            val remoteAttachment = randomAttachment()
            controller.performMessageAction(Edit(randomMessage(cid = CID, attachments = listOf(remoteAttachment))))

            controller.removeAttachment(remoteAttachment)

            assertTrue(controller.state.value.attachments.isEmpty())
        }

    @Test
    fun `Given picker attachments When syncAttachments called after recording completes Then recording attachment is preserved`() =
        runTest {
            val controller = Fixture()
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState()
                .get()
            val pickerAttachment = randomAttachment(extraData = mapOf(EXTRA_SOURCE_URI to "uri:1"))
            controller.addAttachments(listOf(pickerAttachment))
            assertEquals(1, controller.state.value.attachments.size)

            // Simulate recording completion by adding another picker attachment
            // (which triggers syncAttachments) — the existing attachment should survive
            val pickerAttachment2 = randomAttachment(extraData = mapOf(EXTRA_SOURCE_URI to "uri:2"))
            controller.addAttachments(listOf(pickerAttachment2))

            assertEquals(2, controller.state.value.attachments.size)
        }

    @Test
    fun `Given clearAttachments called When recording was completed Then recording attachment is also cleared`() =
        runTest {
            val controller = Fixture()
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState()
                .get()
            val pickerAttachment = randomAttachment(extraData = mapOf(EXTRA_SOURCE_URI to "uri:1"))
            controller.addAttachments(listOf(pickerAttachment))

            controller.clearAttachments()

            assertTrue(controller.state.value.attachments.isEmpty())
        }

    @Test
    fun `Given persisted edit session When controller restores Then edit action uses full message from channel state`() =
        runTest {
            val fullMessage = randomMessage(
                id = "msg-1",
                cid = CID,
                text = "original",
                createdAt = Date(),
            )
            val editedText = "edited text"
            val savedStateHandle = SavedStateHandle()
            ComposerSessionRepository(savedStateHandle).save(
                selectedAttachments = emptyList(),
                editMode = ComposerSessionRepository.EditMode(
                    message = fullMessage.copy(text = editedText),
                    attachments = emptyList(),
                ),
            )

            val controller = Fixture(savedStateHandle = savedStateHandle)
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState()
                .givenMessageById(fullMessage)
                .get()
            advanceUntilIdle()

            assertEquals(editedText, controller.messageInput.value.text)
            val action = controller.state.value.action
            assertTrue(action is Edit)
            assertEquals(fullMessage.id, (action as Edit).message.id)
            assertEquals(fullMessage.createdAt, action.message.createdAt)
        }

    @Test
    fun `Given persisted edit session When channel state has no message Then edit action falls back to stripped message`() =
        runTest {
            val messageId = "msg-2"
            val editedText = "edited text"
            val savedStateHandle = SavedStateHandle()
            ComposerSessionRepository(savedStateHandle).save(
                selectedAttachments = emptyList(),
                editMode = ComposerSessionRepository.EditMode(
                    message = Message(id = messageId, cid = CID, text = editedText),
                    attachments = emptyList(),
                ),
            )

            val controller = Fixture(savedStateHandle = savedStateHandle)
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState()
                .get()
            advanceUntilIdle()

            assertEquals(editedText, controller.messageInput.value.text)
            val action = controller.state.value.action
            assertTrue(action is Edit)
            assertEquals(messageId, (action as Edit).message.id)
            assertNull(action.message.createdAt)
        }

    @Test
    fun `Given in-flight link preview When clearData called Then late resolution does not leak into state`() = runTest {
        // Given
        val url = "https://example.com"
        val resolveSignal = CompletableDeferred<Unit>()
        val pendingCall = MockRetrofitCall(
            scope = this,
            result = Result.Success(randomAttachment()),
            doWork = { resolveSignal.await() },
        )
        val fixture = Fixture()
            .givenConfig(MessageComposerController.Config(linkPreviewEnabled = true))
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState()
            .givenInheritedScope(this)
        whenever(fixture.chatClient.enrichUrl(any())) doReturn pendingCall
        val controller = fixture.get()
        controller.setMessageInput(url)
        advanceUntilIdle()
        // Resolution is in-flight, preview not yet in state.
        assertNull(controller.state.value.linkPreview)

        // When: user sends (via clearData) before resolution completes, then the
        // enrichment response arrives late.
        controller.clearData()
        resolveSignal.complete(Unit)
        // Flush the pending continuation of the in-flight resolve without advancing
        // virtual time past the next debounce tick (which would clear state anyway).
        runCurrent()

        // Then: the late response must not leak into the composer state.
        assertNull(controller.state.value.linkPreview)
        advanceUntilIdle()
    }

    @Test
    fun `Given URL typed When sendMessage called before link preview resolves Then skipEnrichUrl is false`() = runTest {
        // Given
        val url = "https://example.com"
        val sentMessage = randomMessage(cid = CID, text = url)
        val fixture = Fixture()
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState()
            .givenSendMessage(sentMessage)
        val controller = fixture.get()
        controller.setMessageInput(url)
        // Do NOT advanceUntilIdle — debounce hasn't fired, no link preview resolved.

        // When
        controller.sendMessage(Message(cid = CID, text = url), mock())
        advanceUntilIdle()

        // Then: backend should enrich (skipEnrichUrl = false) because the user never
        // dismissed the preview — it just hadn't resolved yet.
        val messageCaptor = argumentCaptor<Message>()
        verify(fixture.chatClient).sendMessage(
            eq(CHANNEL_TYPE),
            eq(CHANNEL_ID),
            messageCaptor.capture(),
            eq(false),
        )
        assertFalse(messageCaptor.firstValue.skipEnrichUrl)
    }

    @Test
    fun `Given dismissed link preview When sendMessage called Then skipEnrichUrl is true`() = runTest {
        // Given
        val url = "https://example.com"
        val sentMessage = randomMessage(cid = CID, text = url)
        val fixture = Fixture()
            .givenConfig(MessageComposerController.Config(linkPreviewEnabled = true))
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState()
            .givenSendMessage(sentMessage)
            .givenInheritedScope(this)
        whenever(fixture.chatClient.enrichUrl(any())) doReturn randomAttachment().asCall()
        val controller = fixture.get()
        controller.setMessageInput(url)
        advanceUntilIdle()
        assertNotNull(controller.state.value.linkPreview)

        // When: user explicitly dismisses the preview, then sends.
        controller.cancelLinkPreview()
        controller.sendMessage(Message(cid = CID, text = url), mock())
        advanceUntilIdle()

        // Then: backend should skip enrichment because the user dismissed the preview.
        val messageCaptor = argumentCaptor<Message>()
        verify(fixture.chatClient).sendMessage(
            eq(CHANNEL_TYPE),
            eq(CHANNEL_ID),
            messageCaptor.capture(),
            eq(false),
        )
        assertTrue(messageCaptor.firstValue.skipEnrichUrl)
    }

    @Test
    fun `Given dismissed link preview When text changed and sendMessage called Then skipEnrichUrl is false`() = runTest {
        // Given
        val url = "https://example.com"
        val newUrl = "https://getstream.io"
        val sentMessage = randomMessage(cid = CID, text = newUrl)
        val fixture = Fixture()
            .givenConfig(MessageComposerController.Config(linkPreviewEnabled = true))
            .givenAppSettings()
            .givenAudioPlayer(mock())
            .givenClientState(randomUser())
            .givenGlobalState()
            .givenChannelState()
            .givenSendMessage(sentMessage)
            .givenInheritedScope(this)
        whenever(fixture.chatClient.enrichUrl(any())) doReturn randomAttachment().asCall()
        val controller = fixture.get()
        controller.setMessageInput(url)
        advanceUntilIdle()
        controller.cancelLinkPreview()

        // When: user types a new URL after dismissing, then sends.
        controller.setMessageInput(newUrl)
        controller.sendMessage(Message(cid = CID, text = newUrl), mock())
        advanceUntilIdle()

        // Then: the text change resets the dismissal — backend should enrich.
        val messageCaptor = argumentCaptor<Message>()
        verify(fixture.chatClient).sendMessage(
            eq(CHANNEL_TYPE),
            eq(CHANNEL_ID),
            messageCaptor.capture(),
            eq(false),
        )
        assertFalse(messageCaptor.firstValue.skipEnrichUrl)
    }

    @Test
    fun `Given dismissed link preview When same URL text edited and sendMessage called Then skipEnrichUrl is true`() =
        runTest {
            // Given
            val url = "https://example.com"
            val sentMessage = randomMessage(cid = CID, text = "$url hello")
            val fixture = Fixture()
                .givenConfig(MessageComposerController.Config(linkPreviewEnabled = true))
                .givenAppSettings()
                .givenAudioPlayer(mock())
                .givenClientState(randomUser())
                .givenGlobalState()
                .givenChannelState()
                .givenSendMessage(sentMessage)
                .givenInheritedScope(this)
            whenever(fixture.chatClient.enrichUrl(any())) doReturn randomAttachment().asCall()
            val controller = fixture.get()
            controller.setMessageInput(url)
            advanceUntilIdle()
            controller.cancelLinkPreview()

            // When: user continues typing non-URL text after dismissing, then sends.
            controller.setMessageInput("$url hello")
            controller.sendMessage(Message(cid = CID, text = "$url hello"), mock())
            advanceUntilIdle()

            // Then: the dismissal is sticky — the URL didn't change, so backend should skip.
            val messageCaptor = argumentCaptor<Message>()
            verify(fixture.chatClient).sendMessage(
                eq(CHANNEL_TYPE),
                eq(CHANNEL_ID),
                messageCaptor.capture(),
                eq(false),
            )
            assertTrue(messageCaptor.firstValue.skipEnrichUrl)
        }

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
        private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ) {

        private val clientState: ClientState = mock()
        private val channelState: ChannelState = mock()
        private val globalState: GlobalState = mock()
        private var inheritedScope: CoroutineScope = TestScope()
        val mediaRecorder: StreamMediaRecorder = mock()
        private var config = MessageComposerController.Config()
        private var userLookupHandler: UserLookupHandler = mock {
            onBlocking { handleUserLookup(any()) } doReturn emptyList()
        }

        fun givenInheritedScope(scope: CoroutineScope) = apply {
            this.inheritedScope = scope
        }

        fun givenAppSettings(appSettings: AppSettings = defaultAppSettings()) = apply {
            whenever(chatClient.getAppSettings()) doReturn appSettings
        }

        fun givenConfig(config: MessageComposerController.Config) = apply {
            this.config = config
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

        fun givenMessageById(message: Message) = apply {
            whenever(channelState.getMessageById(eq(message.id))) doReturn message
        }

        fun givenEditMessage(message: Message) = apply {
            whenever(chatClient.editMessage(any(), any(), any())) doReturn message.asCall()
        }

        fun givenDeleteMessage(message: Message) = apply {
            whenever(chatClient.deleteMessage(any(), any())) doReturn message.asCall()
        }

        fun givenDraftMessageStubs(): Fixture = apply {
            whenever(chatClient.createDraftMessage(any(), any(), any())) doAnswer { invocation ->
                (invocation.arguments[2] as DraftMessage).asCall()
            }
            whenever(chatClient.deleteDraftMessages(any(), any(), any())) doReturn Unit.asCall()
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
                userLookupHandler = userLookupHandler,
                fileToUri = mock(),
                config = config,
                globalState = MutableStateFlow(globalState),
                savedStateHandle = savedStateHandle,
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
        private const val MODERATION_SET = "moderation_set"
    }
}
