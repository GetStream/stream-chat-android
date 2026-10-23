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

import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.Date

@ExperimentalCoroutinesApi
internal class ChannelStateLegacyImplTruncationTest {

    private lateinit var channelState: ChannelStateLegacyImpl

    @BeforeEach
    fun setUp() {
        channelState = ChannelStateLegacyImpl(
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            userFlow = MutableStateFlow(currentUser),
            latestUsers = MutableStateFlow(mapOf(currentUser.id to currentUser)),
            activeLiveLocations = MutableStateFlow(emptyList()),
            baseMessageLimit = null,
            now = { testCoroutines.dispatcher.scheduler.currentTime },
        )
    }

    @Test
    fun `removeTruncatedMessages compares the server creation date and keeps messages not created yet`() = runTest {
        val truncatedAt = Date(2_500)
        val beforeOnServer = message(index = 1, createdAt = Date(1_000), createdLocallyAt = Date(3_000))
        val pending = message(index = 2, createdAt = null, createdLocallyAt = Date(2_000))
        val atTruncation = message(index = 3, createdAt = truncatedAt)
        channelState.setMessages(listOf(beforeOnServer, pending, atTruncation))

        channelState.removeTruncatedMessages(truncatedAt)

        assertEquals(listOf("message_2", "message_3"), channelState.messages.value.map { it.id }.sorted())
    }

    @Test
    fun `removeTruncatedMessages also filters the cached latest messages`() = runTest {
        val old = message(index = 1, createdAt = Date(1_000))
        val kept = message(index = 2, createdAt = Date(3_000))
        channelState.updateCachedLatestMessages(listOf(old, kept).associateBy(Message::id))

        channelState.removeTruncatedMessages(Date(2_000))

        assertEquals(setOf("message_2"), channelState.cachedLatestMessages.value.keys)
    }

    private fun message(index: Int, createdAt: Date?, createdLocallyAt: Date? = null) = randomMessage(
        id = "message_$index",
        cid = CID,
        user = currentUser,
        createdAt = createdAt,
        createdLocallyAt = createdLocallyAt,
        parentId = null,
        showInChannel = true,
        deletedAt = null,
        shadowed = false,
    )

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        private const val CHANNEL_TYPE = "messaging"
        private const val CHANNEL_ID = "123"
        private const val CID = "messaging:123"

        private val currentUser = User(id = "tom", name = "Tom")
    }
}
