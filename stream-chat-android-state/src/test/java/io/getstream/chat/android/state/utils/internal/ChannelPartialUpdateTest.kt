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

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.toChannelData
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

internal class ChannelPartialUpdateTest {

    private val partial = Channel(
        id = randomString(),
        type = randomString(),
        name = "New name",
        image = "new.png",
        frozen = true,
        team = "team1",
        createdAt = randomDate(),
        updatedAt = randomDate(),
        extraData = mapOf("group" to "vip", "disabled" to true),
    )

    @Test
    fun `merging a partial update applies the fields it carries`() {
        val local = randomChannel(frozen = false, extraData = emptyMap())

        val merged = local.mergePartialUpdate(partial)

        assertEquals("New name", merged.name)
        assertEquals("new.png", merged.image)
        assertEquals(true, merged.frozen)
        assertEquals("team1", merged.team)
        assertEquals(partial.createdAt, merged.createdAt)
        assertEquals(partial.updatedAt, merged.updatedAt)
    }

    @Test
    fun `merging a partial update keeps the fields it does not carry`() {
        val local = randomChannel(members = listOf(randomMember()), lastMessageAt = randomDate())

        val merged = local.mergePartialUpdate(partial)

        assertEquals(local.id, merged.id)
        assertEquals(local.type, merged.type)
        assertEquals(local.members, merged.members)
        assertEquals(local.read, merged.read)
        assertEquals(local.config, merged.config)
        assertEquals(local.membership, merged.membership)
        assertEquals(local.lastMessageAt, merged.lastMessageAt)
    }

    @Test
    fun `merging a partial update takes a later last message time from the event`() {
        val local = randomChannel(lastMessageAt = Date(1_000))

        val merged = local.mergePartialUpdate(partial.copy(lastMessageAt = Date(2_000)))

        assertEquals(Date(2_000), merged.lastMessageAt)
    }

    @Test
    fun `merging a partial update keeps a later local last message time`() {
        val local = randomChannel(lastMessageAt = Date(2_000))

        val merged = local.mergePartialUpdate(partial.copy(lastMessageAt = Date(1_000)))

        assertEquals(Date(2_000), merged.lastMessageAt)
    }

    @Test
    fun `merging a partial update replaces the custom data and keeps the server fields`() {
        val local = randomChannel(
            extraData = mapOf(
                "group" to "other",
                "touched_at" to "2026-10-01T00:00:00.000Z",
                "disabled" to false,
                "muted" to true,
                "truncated_at" to "2026-09-01T00:00:00.000Z",
            ),
        )

        val merged = local.mergePartialUpdate(partial)

        val expected = mapOf(
            "group" to "vip",
            "disabled" to true,
            "muted" to true,
            "truncated_at" to "2026-09-01T00:00:00.000Z",
        )
        assertEquals(expected, merged.extraData)
    }

    @Test
    fun `merging a partial update into channel data matches merging it into the channel`() {
        val local = randomChannel(extraData = mapOf("group" to "other", "muted" to true))

        val merged = local.toChannelData().mergePartialUpdate(partial)

        assertEquals(local.mergePartialUpdate(partial).toChannelData(), merged)
    }
}
