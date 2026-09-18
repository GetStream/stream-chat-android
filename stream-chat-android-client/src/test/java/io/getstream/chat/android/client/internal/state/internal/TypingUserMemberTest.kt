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

package io.getstream.chat.android.client.internal.state.internal

import io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal.TypingEventPruner
import io.getstream.chat.android.client.test.randomTypingStartEvent
import io.getstream.chat.android.models.MemberInfo
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.models.TypingUser
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

/**
 * Covers that the membership carried by a typing event reaches [TypingEvent.typingUsers], which is the only place a
 * channel-specific nickname can be read from while a user is typing.
 */
internal class TypingUserMemberTest {

    private val channelId = randomString()

    @Test
    fun `the member on a typing event reaches the typing users`() = runTest {
        var typing: TypingEvent? = null
        val pruner = TypingEventPruner(
            channelId = channelId,
            coroutineScope = this,
            onUpdated = { _, typingEvent -> typing = typingEvent },
        )
        val member = MemberInfo(channelRole = "channel_member", extraData = mapOf("nickname" to "Padme"))
        val event = randomTypingStartEvent(user = randomUser(id = "leandro"), member = member)

        pruner.processEvent(event.user.id, event)

        typing?.typingUsers shouldBeEqualTo listOf(TypingUser(user = event.user, member = member))
    }

    @Test
    fun `a typing event without a member yields a typing user without one`() = runTest {
        var typing: TypingEvent? = null
        val pruner = TypingEventPruner(
            channelId = channelId,
            coroutineScope = this,
            onUpdated = { _, typingEvent -> typing = typingEvent },
        )
        val event = randomTypingStartEvent(member = null)

        pruner.processEvent(event.user.id, event)

        typing?.typingUsers shouldBeEqualTo listOf(TypingUser(user = event.user, member = null))
    }

    @Test
    fun `the deprecated users list stays in sync with the typing users`() = runTest {
        var typing: TypingEvent? = null
        val pruner = TypingEventPruner(
            channelId = channelId,
            coroutineScope = this,
            onUpdated = { _, typingEvent -> typing = typingEvent },
        )
        val first = randomTypingStartEvent(user = randomUser(id = "user-1"), member = MemberInfo("channel_member"))
        val second = randomTypingStartEvent(user = randomUser(id = "user-2"), member = null)

        pruner.processEvent(first.user.id, first)
        pruner.processEvent(second.user.id, second)

        @Suppress("DEPRECATION")
        typing?.users shouldBeEqualTo typing?.typingUsers?.map(TypingUser::user)
    }
}
