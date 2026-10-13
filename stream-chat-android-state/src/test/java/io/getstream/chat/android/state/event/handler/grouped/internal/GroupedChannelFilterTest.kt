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

import io.getstream.chat.android.models.Filters
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Date
import java.util.concurrent.TimeUnit

internal class GroupedChannelFilterTest {

    private val now = Date(1_800_000_000_000)

    @Test
    fun `neutral filter matches any channel`() {
        assertTrue(Filters.neutral().matchesGroupedChannel(group = null, queryTimestamp = null, now = now))
    }

    @Test
    fun `group conditions compare against the channel group`() {
        assertTrue(Filters.eq("group", "new").matchesGroupedChannel("new", daysAgo(1), now))
        assertFalse(Filters.eq("group", "new").matchesGroupedChannel("bee", daysAgo(1), now))
        assertTrue(Filters.`in`("group", listOf("new", "campaign")).matchesGroupedChannel("campaign", null, now))
        assertFalse(Filters.`in`("group", listOf("new", "campaign")).matchesGroupedChannel("bee", null, now))
        assertTrue(Filters.ne("group", "bee").matchesGroupedChannel("new", null, now))
        assertFalse(Filters.ne("group", "bee").matchesGroupedChannel("bee", null, now))
    }

    @Test
    fun `relative query_timestamp cutoff keeps channels at or after it`() {
        val filter = Filters.greaterThanEquals("query_timestamp", "now-14d")

        assertTrue(filter.matchesGroupedChannel("expired", daysAgo(13), now))
        assertTrue(filter.matchesGroupedChannel("expired", daysAgo(14), now))
        assertFalse(filter.matchesGroupedChannel("expired", daysAgo(15), now))
        assertFalse(filter.matchesGroupedChannel("expired", queryTimestamp = null, now = now))
    }

    @Test
    fun `relative cutoff without the now prefix and a date cutoff are supported`() {
        assertFalse(Filters.greaterThanEquals("query_timestamp", "7d").matchesGroupedChannel("ended", daysAgo(8), now))
        assertTrue(Filters.greaterThanEquals("query_timestamp", daysAgo(7)).matchesGroupedChannel("ended", daysAgo(6), now))
        assertFalse(Filters.greaterThanEquals("query_timestamp", daysAgo(7)).matchesGroupedChannel("ended", daysAgo(8), now))
    }

    @Test
    fun `conditions that cannot be evaluated locally match`() {
        assertTrue(
            Filters.greaterThanEquals("query_timestamp", "2026-09-01T00:00:00Z").matchesGroupedChannel("new", null, now),
        )
        assertTrue(Filters.eq("team", "blue").matchesGroupedChannel("new", null, now))
    }

    @Test
    fun `combined all tab filter applies each group's cutoff`() {
        val all = Filters.or(
            Filters.and(Filters.eq("group", "ended"), Filters.greaterThanEquals("query_timestamp", "now-7d")),
            Filters.`in`("group", listOf("campaign", "new")),
            Filters.and(Filters.eq("group", "current"), Filters.greaterThanEquals("query_timestamp", "now-365d")),
        )

        assertTrue(all.matchesGroupedChannel("ended", daysAgo(6), now))
        assertFalse(all.matchesGroupedChannel("ended", daysAgo(8), now))
        assertTrue(all.matchesGroupedChannel("new", daysAgo(400), now))
        assertTrue(all.matchesGroupedChannel("current", daysAgo(300), now))
        assertFalse(all.matchesGroupedChannel("current", daysAgo(400), now))
        assertFalse(all.matchesGroupedChannel("bee", daysAgo(1), now))
    }

    private fun daysAgo(days: Long): Date = Date(now.time - TimeUnit.DAYS.toMillis(days))
}
