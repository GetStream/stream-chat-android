/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.models.AppSettings
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.User
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

@ExperimentalCoroutinesApi
internal class MessageComposerControllerTests {

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
            .givenChannelState(configState = configFlow)
            .get()
        // then
        controller.state.value.pollsEnabled `should be` true
    }

    private class Fixture(
        private val chatClient: ChatClient = mock(),
        private val cid: String = CID,
    ) {

        private val clientState: ClientState = mock()
        private val channelState: ChannelState = mock()

        fun givenAppSettings(appSettings: AppSettings) = apply {
            whenever(chatClient.getAppSettings()) doReturn appSettings
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

        fun get(): MessageComposerController {
            return MessageComposerController(
                channelCid = cid,
                chatClient = chatClient,
                channelState = MutableStateFlow(channelState),
                mediaRecorder = mock(),
                userLookupHandler = mock(),
                fileToUri = mock(),
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
