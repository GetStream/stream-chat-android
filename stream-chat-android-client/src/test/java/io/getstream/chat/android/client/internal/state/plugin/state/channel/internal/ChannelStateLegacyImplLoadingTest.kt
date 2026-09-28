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

package io.getstream.chat.android.client.internal.state.plugin.state.channel.internal

import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessagesState
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.Date

@ExperimentalCoroutinesApi
internal class ChannelStateLegacyImplLoadingTest {

    private lateinit var channelState: ChannelStateLegacyImpl

    @BeforeEach
    fun setUp() {
        channelState = ChannelStateLegacyImpl(
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            userFlow = MutableStateFlow<User?>(currentUser),
            latestUsers = MutableStateFlow(mapOf(currentUser.id to currentUser)),
            activeLiveLocations = MutableStateFlow(emptyList()),
            baseMessageLimit = null,
            now = ::currentTime,
        )
    }

    @Test
    fun `setLoadingIfEmpty on a channel without data should show the loading state`() = runTest {
        channelState.setLoadingIfEmpty()

        channelState.loading.value `should be equal to` true
        channelState.messagesState.value `should be equal to` MessagesState.Loading
    }

    @Test
    fun `setLoadingIfEmpty on a channel with data should not show the loading state`() = runTest {
        channelState.setChannelData(ChannelData(type = CHANNEL_TYPE, id = CHANNEL_ID))

        channelState.setLoadingIfEmpty()

        channelState.loading.value `should be equal to` false
        channelState.messagesState.value `should be equal to` MessagesState.OfflineNoResults
    }

    @Test
    fun `messagesState should not report no results while the loaded messages are still propagating`() =
        runTest(StandardTestDispatcher(testCoroutines.dispatcher.scheduler)) {
            val emissions = mutableListOf<MessagesState>()
            backgroundScope.launch { channelState.messagesState.collect { emissions += it } }
            channelState.setLoadingIfEmpty()
            runCurrent()

            channelState.setMessages(createMessages(3))
            channelState.setLoading(false)
            runCurrent()

            val afterLoading = emissions.dropWhile { it != MessagesState.Loading }
            afterLoading.contains(MessagesState.OfflineNoResults) `should be equal to` false
            (afterLoading.last() is MessagesState.Result) `should be equal to` true
        }

    private fun createMessages(count: Int): List<Message> {
        val now = currentTime()
        return (1..count).map { i ->
            randomMessage(
                id = "message_$i",
                cid = CID,
                createdAt = Date(now + i * 1000L),
                createdLocallyAt = null,
                parentId = null,
                shadowed = false,
                deletedAt = null,
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

        private val currentUser = User(id = "tom", name = "Tom")

        private fun currentTime() = testCoroutines.dispatcher.scheduler.currentTime
    }
}
