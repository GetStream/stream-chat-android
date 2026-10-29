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

package io.getstream.chat.android.client.internal.state.plugin.logic.channel.thread.internal

import io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal.TypingEventPruner
import io.getstream.chat.android.client.internal.state.plugin.state.channel.thread.internal.ThreadMutableState
import io.getstream.chat.android.client.test.randomTypingStartEvent
import io.getstream.chat.android.client.test.randomTypingStopEvent
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@Suppress("DEPRECATION")
internal class ThreadLogicTypingTest {

    private val parentId = randomString()
    private val channelId = randomString()

    @Test
    fun `A typing start in the thread adds the user to the thread typing state`() = runTest {
        val state = ThreadMutableState(parentId, backgroundScope)
        val logic = ThreadLogic(ThreadStateLogic(state), backgroundScope)
        val user = randomUser()

        logic.handleTypingEvents(listOf(randomTypingStartEvent(user = user, channelId = channelId, parentId = parentId)))

        assertEquals(listOf(user), state.typing.value.users)
        assertEquals(channelId, state.typing.value.channelId)
    }

    @Test
    fun `A typing stop in the thread removes the user`() = runTest {
        val state = ThreadMutableState(parentId, backgroundScope)
        val logic = ThreadLogic(ThreadStateLogic(state), backgroundScope)
        val user = randomUser()

        logic.handleTypingEvents(
            listOf(
                randomTypingStartEvent(user = user, channelId = channelId, parentId = parentId),
                randomTypingStopEvent(user = user, channelId = channelId, parentId = parentId),
            ),
        )

        assertEquals(emptyList<Any>(), state.typing.value.users)
    }

    @Test
    fun `A typing start from another thread or the channel is ignored`() = runTest {
        val state = ThreadMutableState(parentId, backgroundScope)
        val logic = ThreadLogic(ThreadStateLogic(state), backgroundScope)

        logic.handleTypingEvents(
            listOf(
                randomTypingStartEvent(channelId = channelId, parentId = randomString()),
                randomTypingStartEvent(channelId = channelId, parentId = null),
            ),
        )

        assertEquals(emptyList<Any>(), state.typing.value.users)
    }

    @Test
    fun `A typing stop naming another thread or the channel still removes the user`() = runTest {
        val state = ThreadMutableState(parentId, backgroundScope)
        val logic = ThreadLogic(ThreadStateLogic(state), backgroundScope)
        val user = randomUser()
        val otherUser = randomUser()

        logic.handleTypingEvents(
            listOf(
                randomTypingStartEvent(user = user, channelId = channelId, parentId = parentId),
                randomTypingStartEvent(user = otherUser, channelId = channelId, parentId = parentId),
                randomTypingStopEvent(user = user, channelId = channelId, parentId = randomString()),
                randomTypingStopEvent(user = otherUser, channelId = channelId, parentId = null),
            ),
        )

        assertEquals(emptyList<Any>(), state.typing.value.users)
    }

    @Test
    fun `A typing start without a stop expires`() = runTest {
        val state = ThreadMutableState(parentId, backgroundScope)
        val logic = ThreadLogic(ThreadStateLogic(state), backgroundScope)

        logic.handleTypingEvents(listOf(randomTypingStartEvent(channelId = channelId, parentId = parentId)))
        advanceTimeBy(TypingEventPruner.DEFAULT_DELAY_TIME_MS + 1)

        assertEquals(emptyList<Any>(), state.typing.value.users)
    }
}
