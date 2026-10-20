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

package io.getstream.chat.android.state.utils.internal

import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ChannelVisibilityTest {

    @Test
    fun `A regular message un-hides the channel`() {
        assertTrue(message(type = MessageType.REGULAR).unhidesChannel(emptyMap(), emptyList()))
    }

    @Test
    fun `A reply un-hides the channel`() {
        assertTrue(message(type = MessageType.REPLY).unhidesChannel(emptyMap(), emptyList()))
    }

    @Test
    fun `A system message keeps the channel hidden`() {
        assertFalse(message(type = MessageType.SYSTEM).unhidesChannel(emptyMap(), emptyList()))
    }

    @Test
    fun `A shadowed message keeps the channel hidden`() {
        assertFalse(message(shadowed = true).unhidesChannel(emptyMap(), emptyList()))
    }

    @Test
    fun `A campaign message keeps the channel hidden`() {
        val message = message(extraData = mapOf("created_by_campaign_id" to randomString()))

        assertFalse(message.unhidesChannel(emptyMap(), emptyList()))
    }

    @Test
    fun `A message in a blocked channel keeps the channel hidden`() {
        assertFalse(message().unhidesChannel(mapOf("blocked" to true), emptyList()))
    }

    @Test
    fun `A message from a blocked user keeps the channel hidden`() {
        val message = message()

        assertFalse(message.unhidesChannel(emptyMap(), listOf(message.user.id)))
    }

    @Test
    fun `A channel from an event takes the stored hidden and blocked state`() {
        val stored = randomChannel(hidden = true, extraData = mapOf("blocked" to true))
        val fromEvent = randomChannel(hidden = null, extraData = mapOf("group" to "vip"))

        val result = fromEvent.withUserStateOf(stored)

        assertEquals(true, result.hidden)
        assertEquals(mapOf("blocked" to true, "group" to "vip"), result.extraData)
    }

    @Test
    fun `A channel from an event keeps the hidden and blocked state it carries`() {
        val stored = randomChannel(hidden = true, extraData = mapOf("blocked" to true))
        val fromEvent = randomChannel(hidden = false, extraData = mapOf("blocked" to false))

        val result = fromEvent.withUserStateOf(stored)

        assertEquals(false, result.hidden)
        assertEquals(mapOf("blocked" to false), result.extraData)
    }

    private fun message(
        type: String = MessageType.REGULAR,
        shadowed: Boolean = false,
        extraData: Map<String, Any> = emptyMap(),
    ): Message = randomMessage(type = type, shadowed = shadowed, extraData = extraData, user = randomUser())
}
