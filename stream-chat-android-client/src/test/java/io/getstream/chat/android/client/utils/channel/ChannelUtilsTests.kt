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

package io.getstream.chat.android.client.utils.channel

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class ChannelUtilsTests {

    @Test
    fun `should return provided channelId if not blank`() {
        val channelId = "123"
        val memberIds = listOf("user1", "user2")
        generateChannelIdIfNeeded(channelId, memberIds) shouldBeEqualTo channelId
    }

    @Test
    fun `should generate member-based channelId if provided channelId is blank`() {
        val channelId = ""
        val memberIds = listOf("user1", "user2")
        generateChannelIdIfNeeded(channelId, memberIds) shouldBeEqualTo "!members-user1, user2"
    }

    @Test
    fun `should generate member-based channelId with single member if provided channelId is blank`() {
        val channelId = ""
        val memberIds = listOf("user1")
        generateChannelIdIfNeeded(channelId, memberIds) shouldBeEqualTo "!members-user1"
    }

    @Test
    fun `should generate member-based channelId with no members if provided channelId is blank`() {
        val channelId = ""
        val memberIds = emptyList<String>()
        generateChannelIdIfNeeded(channelId, memberIds) shouldBeEqualTo "!members-"
    }
}
