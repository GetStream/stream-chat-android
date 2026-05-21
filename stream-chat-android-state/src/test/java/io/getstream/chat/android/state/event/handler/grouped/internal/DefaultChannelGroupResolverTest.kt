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

package io.getstream.chat.android.state.event.handler.grouped.internal

import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DefaultChannelGroupResolverTest {

    @Test
    fun `Given a channel with an explicit group When resolved Then returns the group plus the all sentinel`() {
        val channel = randomChannel(extraData = mapOf("group" to "vip"))
        val resolver = DefaultChannelGroupResolver()

        val result = resolver.resolve(channel, currentGroup = randomString())

        assertEquals(setOf("vip", "all"), result)
    }

    @Test
    fun `Given a channel with no group extra When resolved Then returns only the all sentinel`() {
        val channel = randomChannel(extraData = emptyMap())
        val resolver = DefaultChannelGroupResolver()

        val result = resolver.resolve(channel, currentGroup = randomString())

        assertEquals(setOf("all"), result)
    }

    @Test
    fun `Given a custom group field name When resolved Then reads that field`() {
        val channel = randomChannel(extraData = mapOf("tier" to "gold", "group" to "ignored"))
        val resolver = DefaultChannelGroupResolver(groupFieldName = "tier")

        val result = resolver.resolve(channel, currentGroup = randomString())

        assertEquals(setOf("gold", "all"), result)
    }

    @Test
    fun `Given the all sentinel is disabled When resolved Then returns only the explicit group`() {
        val channel = randomChannel(extraData = mapOf("group" to "vip"))
        val resolver = DefaultChannelGroupResolver(allGroupKey = null)

        val result = resolver.resolve(channel, currentGroup = randomString())

        assertEquals(setOf("vip"), result)
    }

    @Test
    fun `Given a non-string group extra When resolved Then ignores it and returns only the all sentinel`() {
        val channel = randomChannel(extraData = mapOf("group" to 42))
        val resolver = DefaultChannelGroupResolver()

        val result = resolver.resolve(channel, currentGroup = randomString())

        assertEquals(setOf("all"), result)
    }
}
