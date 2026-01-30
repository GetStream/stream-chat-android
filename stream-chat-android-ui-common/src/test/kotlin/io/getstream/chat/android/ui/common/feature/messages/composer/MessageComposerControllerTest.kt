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

import io.getstream.chat.android.client.ChatClient
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
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.state.plugin.state.global.GlobalState
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.Mention
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.MentionType
import io.getstream.chat.android.ui.common.state.messages.MessageInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
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
            pattern.matches(url) `should be equal to` true
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
            pattern.matches(url) `should be equal to` false
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
        controller.state.value.hasCommands `should be` true
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
        controller.state.value.pollsEnabled `should be` true
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
        controller.messageInput.value.text `should be equal to` "Hello @John Doe "
        controller.state.value.selectedMentions.size `should be equal to` 1
        controller.state.value.selectedMentions `should contain` Mention.User(user)
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
        controller.messageInput.value.text `should be equal to` "Hello @John Doe "
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
        controller.messageInput.value.text `should be equal to` "Notify @Channel Name "
        controller.state.value.selectedMentions.size `should be equal to` 1
        controller.state.value.selectedMentions `should contain` customMention
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
        controller.messageInput.value.text `should be equal to` "Hello @John Doe and @Jane Smith "
        controller.state.value.selectedMentions.size `should be equal to` 2
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
        controller.messageInput.value.source `should be equal to` MessageInput.Source.MentionSelected
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
        controller.selectedAttachments.value `should be equal to` attachments
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
        private val chatClient: ChatClient = mock(),
        private val cid: String = CID,
    ) {

        private val clientState: ClientState = mock()
        private val channelState: ChannelState = mock()
        private val globalState: GlobalState = mock()

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

        fun givenClientState(user: User) = apply {
            whenever(clientState.user) doReturn MutableStateFlow(user)
            whenever(chatClient.clientState) doReturn clientState
        }

        fun givenChannelState(
            channelDataState: StateFlow<ChannelData> = MutableStateFlow(
                value = ChannelData(CHANNEL_ID, CHANNEL_TYPE),
            ),
            configState: StateFlow<Config> = MutableStateFlow(Config()),
            membersState: StateFlow<List<Member>> = MutableStateFlow(emptyList()),
            lastSentMessageDateState: StateFlow<Date?> = MutableStateFlow(null),
        ) = apply {
            whenever(channelState.cid) doReturn cid
            whenever(channelState.channelData) doReturn channelDataState
            whenever(channelState.channelConfig) doReturn configState
            whenever(channelState.members) doReturn membersState
            whenever(channelState.lastSentMessageDate) doReturn lastSentMessageDateState
        }

        fun givenGlobalState(
            channelDrafts: Map<String, DraftMessage> = mapOf(),
            threadDrafts: Map<String, DraftMessage> = mapOf(),
        ) = apply {
            whenever(globalState.channelDraftMessages) doReturn MutableStateFlow(channelDrafts)
            whenever(globalState.threadDraftMessages) doReturn MutableStateFlow(threadDrafts)
        }

        fun get(): MessageComposerController {
            return MessageComposerController(
                channelCid = cid,
                chatClient = chatClient,
                channelState = MutableStateFlow(channelState),
                mediaRecorder = mock(),
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
