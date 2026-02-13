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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.Date

/**
 * Base class for ChannelStateImpl tests providing common setup and utilities.
 */
@ExperimentalCoroutinesApi
internal abstract class ChannelStateImplTestBase {

    protected val userFlow = MutableStateFlow(currentUser)
    protected lateinit var channelState: ChannelStateImpl

    @BeforeEach
    fun setUp() {
        channelState = ChannelStateImpl(
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            currentUser = userFlow,
            latestUsers = MutableStateFlow(mapOf(currentUser.id to currentUser)),
            mutedUsers = MutableStateFlow(emptyList()),
            liveLocations = MutableStateFlow(emptyList()),
            messageLimit = null,
        )
    }

    protected fun createMessage(
        index: Int,
        timestamp: Long = currentTime() + index * 1000L,
        text: String = "Test message $index",
        user: User = currentUser,
        parentId: String? = null,
        showInChannel: Boolean = true,
        shadowed: Boolean = false,
        silent: Boolean = false,
        pinned: Boolean = false,
        pinnedAt: Date? = null,
        replyTo: Message? = null,
    ): Message = randomMessage(
        id = "message_$index",
        cid = CID,
        createdAt = Date(timestamp),
        createdLocallyAt = null,
        text = text,
        user = user,
        parentId = parentId,
        showInChannel = showInChannel,
        shadowed = shadowed,
        silent = silent,
        pinned = pinned,
        pinnedAt = pinnedAt,
        deletedAt = null,
        replyTo = replyTo,
    )

    protected fun createMessages(
        count: Int,
        startIndex: Int = 1,
        baseTimestamp: Long = currentTime(),
    ): List<Message> {
        return (startIndex until startIndex + count).map { i ->
            createMessage(i, timestamp = baseTimestamp + i * 1000L)
        }
    }

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        const val CHANNEL_TYPE = "messaging"
        const val CHANNEL_ID = "123"
        const val CID = "messaging:123"

        val currentUser = User(id = "tom", name = "Tom")

        fun currentTime() = testCoroutines.dispatcher.scheduler.currentTime
    }
}
