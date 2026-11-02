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

package io.getstream.chat.android.models

import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMember
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ChannelTest {

    @Test
    fun `mergeChannelFromEvent should keep members when the update leaves them out`() {
        val members = listOf(randomMember(), randomMember())
        val original = randomChannel(members = members, memberCount = 2)
        val update = randomChannel(members = emptyList(), memberCount = 150)

        val merged = original.mergeChannelFromEvent(update)

        assertEquals(members, merged.members)
        assertEquals(150, merged.memberCount)
    }

    @Test
    fun `mergeChannelFromEvent should clear members when the update has none left`() {
        val original = randomChannel(members = listOf(randomMember()), memberCount = 1)
        val update = randomChannel(members = emptyList(), memberCount = 0)

        val merged = original.mergeChannelFromEvent(update)

        assertEquals(emptyList<Member>(), merged.members)
    }

    @Test
    fun `mergeChannelFromEvent should take the members the update lists`() {
        val original = randomChannel(members = listOf(randomMember()), memberCount = 1)
        val members = listOf(randomMember(), randomMember())
        val update = randomChannel(members = members, memberCount = 2)

        val merged = original.mergeChannelFromEvent(update)

        assertEquals(members, merged.members)
    }
}
