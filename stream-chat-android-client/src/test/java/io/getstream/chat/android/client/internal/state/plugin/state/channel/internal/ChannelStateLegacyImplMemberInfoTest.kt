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

import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.Date
import kotlin.time.Duration.Companion.hours

/**
 * Tests for refreshing the member snapshot carried by messages on the legacy channel state implementation. The backend
 * does not emit `message.updated` when a membership changes, so the snapshot has to be refreshed by the SDK.
 */
internal class ChannelStateLegacyImplMemberInfoTest {

    private val userFlow = MutableStateFlow(currentUser)

    @Test
    fun `updateMessagesMemberInfo refreshes the member snapshot on the author's messages`() = runTest {
        // given
        val state = channelState()
        val author = randomUser(id = "author")
        state.setMessages(listOf(authoredMessage(author)))
        val member = randomMember(user = author, channelRole = "channel_moderator")
            .copy(notificationsMuted = true, extraData = mapOf("flair" to "gold"))
        // when
        state.updateMessagesMemberInfo(member)
        // then
        val updated = state.messages.value.first()
        assertEquals("channel_moderator", updated.member?.channelRole)
        assertEquals(true, updated.member?.notificationsMuted)
        assertEquals(mapOf("flair" to "gold"), updated.member?.extraData)
    }

    @Test
    fun `updateMessagesMemberInfo leaves messages of other users untouched`() = runTest {
        // given
        val state = channelState()
        val otherAuthor = randomUser(id = "other_author")
        state.setMessages(listOf(authoredMessage(otherAuthor)))
        // when
        state.updateMessagesMemberInfo(randomMember(user = randomUser(id = "author")))
        // then
        assertTrue(state.messages.value.all { it.member == null })
    }

    @Test
    fun `updateMessagesMemberInfo refreshes pinned messages too`() = runTest {
        // given
        val state = channelState()
        val author = randomUser(id = "author")
        val pinnedMessage = authoredMessage(author).copy(
            pinned = true,
            pinnedAt = Date(currentTime()),
            pinExpires = Date(currentTime() + 1.hours.inWholeMilliseconds),
        )
        state.setPinnedMessages(listOf(pinnedMessage))
        // when
        state.updateMessagesMemberInfo(randomMember(user = author, channelRole = "channel_moderator"))
        // then
        assertEquals("channel_moderator", state.pinnedMessages.value.first().member?.channelRole)
    }

    private fun authoredMessage(author: User) = randomMessage(
        cid = CID,
        user = author,
        parentId = null,
        shadowed = false,
        deletedAt = null,
        deletedForMe = false,
        member = null,
    )

    private fun channelState() = ChannelStateLegacyImpl(
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        userFlow = userFlow,
        latestUsers = MutableStateFlow(mapOf(currentUser.id to currentUser)),
        activeLiveLocations = MutableStateFlow(emptyList()),
        baseMessageLimit = null,
        now = ::currentTime,
    )

    private companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        const val CHANNEL_TYPE = "messaging"
        const val CHANNEL_ID = "123"
        const val CID = "messaging:123"

        val currentUser = User(id = "tom", name = "Tom")

        @OptIn(ExperimentalCoroutinesApi::class)
        fun currentTime() = testCoroutines.dispatcher.scheduler.currentTime
    }
}
