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

import io.getstream.chat.android.client.utils.channel.generateChannelIdIfNeeded
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be null`
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class ChannelIdTest {

    @ParameterizedTest
    @MethodSource("validCids")
    fun `fromCid returns a ChannelId for a valid cid`(cid: String) {
        val channelId = ChannelId.fromCid(cid)
        channelId?.cid `should be equal to` cid
    }

    @ParameterizedTest
    @MethodSource("invalidCids")
    fun `fromCid returns null for a malformed cid`(cid: String) {
        ChannelId.fromCid(cid).`should be null`()
    }

    @Test
    fun `fromTypeAndId joins parts and validates the result`() {
        val channelId = ChannelId.fromTypeAndId("messaging", "123")
        channelId?.cid `should be equal to` "messaging:123"
        channelId?.type `should be equal to` "messaging"
        channelId?.id `should be equal to` "123"
    }

    @Test
    fun `fromTypeAndId returns null when a part is blank`() {
        ChannelId.fromTypeAndId("", "123").`should be null`()
        ChannelId.fromTypeAndId("messaging", "").`should be null`()
    }

    @Test
    fun `fromTypeAndId returns null when a part contains a colon`() {
        ChannelId.fromTypeAndId("messaging:foo", "123").`should be null`()
    }

    @ParameterizedTest
    @MethodSource("offlineDistinctMemberIds")
    fun `fromTypeAndId accepts the id generated for a distinct channel created offline`(memberIds: List<String>) {
        val channelId = generateChannelIdIfNeeded(channelId = "", memberIds = memberIds)

        ChannelId.fromTypeAndId("messaging", channelId)?.id `should be equal to` channelId
    }

    companion object {

        @JvmStatic
        fun offlineDistinctMemberIds() = listOf(
            listOf("alice"),
            listOf("alice", "bob"),
            listOf("alice@example.com", "bob.smith", "carol jones"),
        )

        @JvmStatic
        fun validCids() = listOf(
            "messaging:123",
            "a:e",
            "messaging:!members-oNJ1lQqt2b9SKG6raDWRTn4wWLakkFkwvqlUn-EsatU",
            "!members-hash:!members-hash",
        )

        @JvmStatic
        fun invalidCids() = listOf(
            "",
            "   ",
            "messaging 123",
            "messaging123",
            "messaging::123",
            "messaging:",
            ":123",
            ":",
        )
    }
}
