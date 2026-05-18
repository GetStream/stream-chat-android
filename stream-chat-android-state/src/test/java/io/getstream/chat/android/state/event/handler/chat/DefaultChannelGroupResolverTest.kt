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

package io.getstream.chat.android.state.event.handler.chat

import io.getstream.chat.android.randomString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DefaultChannelGroupResolverTest {

    @Test
    fun `Given a channel_custom with an explicit group When resolved Then returns the group plus the all sentinel`() {
        val channelCustom: Map<String, Any> = mapOf("group" to "vip")
        val resolver = DefaultChannelGroupResolver()

        val result = resolver.resolve(channelCustom, channelExtraData = null, currentGroup = randomString())

        assertEquals(setOf("vip", "all"), result)
    }

    @Test
    fun `Given a null channel_custom and null extraData When resolved Then returns only the all sentinel`() {
        val resolver = DefaultChannelGroupResolver()

        val result = resolver.resolve(
            channelCustom = null,
            channelExtraData = null,
            currentGroup = randomString(),
        )

        assertEquals(setOf("all"), result)
    }

    @Test
    fun `Given an empty channel_custom and empty extraData When resolved Then returns only the all sentinel`() {
        val resolver = DefaultChannelGroupResolver()

        val result = resolver.resolve(
            channelCustom = emptyMap(),
            channelExtraData = emptyMap(),
            currentGroup = randomString(),
        )

        assertEquals(setOf("all"), result)
    }

    @Test
    fun `Given a custom group field name When resolved Then reads that field`() {
        val channelCustom: Map<String, Any> = mapOf("tier" to "gold", "group" to "ignored")
        val resolver = DefaultChannelGroupResolver(groupFieldName = "tier")

        val result = resolver.resolve(channelCustom, channelExtraData = null, currentGroup = randomString())

        assertEquals(setOf("gold", "all"), result)
    }

    @Test
    fun `Given the all sentinel is disabled When resolved Then returns only the explicit group`() {
        val channelCustom: Map<String, Any> = mapOf("group" to "vip")
        val resolver = DefaultChannelGroupResolver(allGroupKey = null)

        val result = resolver.resolve(channelCustom, channelExtraData = null, currentGroup = randomString())

        assertEquals(setOf("vip"), result)
    }

    @Test
    fun `Given a non-string group value When resolved Then ignores it and returns only the all sentinel`() {
        val channelCustom: Map<String, Any> = mapOf("group" to 42)
        val resolver = DefaultChannelGroupResolver()

        val result = resolver.resolve(channelCustom, channelExtraData = null, currentGroup = randomString())

        assertEquals(setOf("all"), result)
    }

    @Test
    fun `Given group nested under custom When resolved Then returns the nested group`() {
        val channelCustom: Map<String, Any> = mapOf("custom" to mapOf("group" to "vip"))
        val resolver = DefaultChannelGroupResolver()

        val result = resolver.resolve(channelCustom, channelExtraData = null, currentGroup = randomString())

        assertEquals(setOf("vip", "all"), result)
    }

    @Test
    fun `Given group present in custom and at top level When resolved Then nested custom wins`() {
        val channelCustom: Map<String, Any> = mapOf(
            "custom" to mapOf("group" to "vip"),
            "group" to "other",
        )
        val resolver = DefaultChannelGroupResolver()

        val result = resolver.resolve(channelCustom, channelExtraData = null, currentGroup = randomString())

        assertEquals(setOf("vip", "all"), result)
    }

    @Test
    fun `Given group only in extraData When resolved Then falls back to extraData`() {
        val channelExtraData: Map<String, Any> = mapOf("group" to "vip")
        val resolver = DefaultChannelGroupResolver()

        val result = resolver.resolve(channelCustom = null, channelExtraData = channelExtraData, currentGroup = randomString())

        assertEquals(setOf("vip", "all"), result)
    }

    @Test
    fun `Given group in channel_custom and in extraData When resolved Then channel_custom wins over extraData`() {
        val channelCustom: Map<String, Any> = mapOf("group" to "vip")
        val channelExtraData: Map<String, Any> = mapOf("group" to "other")
        val resolver = DefaultChannelGroupResolver()

        val result = resolver.resolve(channelCustom, channelExtraData, currentGroup = randomString())

        assertEquals(setOf("vip", "all"), result)
    }

    @Test
    fun `Given group in nested custom and in extraData When resolved Then nested custom wins over extraData`() {
        val channelCustom: Map<String, Any> = mapOf("custom" to mapOf("group" to "vip"))
        val channelExtraData: Map<String, Any> = mapOf("group" to "other")
        val resolver = DefaultChannelGroupResolver()

        val result = resolver.resolve(channelCustom, channelExtraData, currentGroup = randomString())

        assertEquals(setOf("vip", "all"), result)
    }

    @Test
    fun `Given non-string extraData group When resolved Then ignores it and returns only the all sentinel`() {
        val channelExtraData: Map<String, Any> = mapOf("group" to 99)
        val resolver = DefaultChannelGroupResolver()

        val result = resolver.resolve(channelCustom = null, channelExtraData = channelExtraData, currentGroup = randomString())

        assertEquals(setOf("all"), result)
    }
}
