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

package io.getstream.chat.android.client.utils.message

import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import java.util.Date

internal class MessageSortComparatorTest {

    @Test
    fun `cross-user confirmed messages sort by server time regardless of local clock`() {
        val userA = randomUser(id = "userA")
        val userB = randomUser(id = "userB")
        // userB's device clock is an hour ahead, but server records the real time
        val serverEarlier = Date(1_000_000L)
        val serverLater = Date(2_000_000L)
        val localFastClock = Date(5_000_000L) // userB's skewed local time
        val messageA = randomMessage(user = userA, createdAt = serverLater, createdLocallyAt = null)
        val messageB = randomMessage(user = userB, createdAt = serverEarlier, createdLocallyAt = localFastClock)

        val sorted = listOf(messageA, messageB).sortedWith(MessageSortComparator)

        // messageB has earlier createdAt, so it should come first
        sorted[0].user.id shouldBeEqualTo "userB"
        sorted[1].user.id shouldBeEqualTo "userA"
    }

    @Test
    fun `same user rapid send preserves local send order`() {
        val user = randomUser(id = "me")
        val localFirst = Date(1_000L)
        val localSecond = Date(2_000L)
        // Both messages have a createdLocallyAt; createdAt values are absent (still pending)
        val m1 = randomMessage(user = user, createdAt = null, createdLocallyAt = localFirst)
        val m2 = randomMessage(user = user, createdAt = null, createdLocallyAt = localSecond)

        val sorted = listOf(m2, m1).sortedWith(MessageSortComparator)

        sorted[0].createdLocallyAt shouldBeEqualTo localFirst
        sorted[1].createdLocallyAt shouldBeEqualTo localSecond
    }

    @Test
    fun `confirmed message stays before still-pending next message`() {
        val user = randomUser(id = "me")
        // M1 sent first, confirmed with createdAt that is higher than M2's createdLocallyAt due to latency
        val localM1 = Date(1_000L)
        val localM2 = Date(2_000L)
        val serverM1 = Date(3_000L) // arrives after localM2 due to round-trip latency
        val m1 = randomMessage(user = user, createdAt = serverM1, createdLocallyAt = localM1)
        val m2 = randomMessage(user = user, createdAt = null, createdLocallyAt = localM2)

        val sorted = listOf(m2, m1).sortedWith(MessageSortComparator)

        // Both have createdLocallyAt and same user → sorted by createdLocallyAt → m1 first
        sorted[0].createdLocallyAt shouldBeEqualTo localM1
        sorted[1].createdLocallyAt shouldBeEqualTo localM2
    }

    @Test
    fun `pending messages without createdAt sort by createdLocallyAt`() {
        val userA = randomUser(id = "userA")
        val userB = randomUser(id = "userB")
        val localA = Date(1_000L)
        val localB = Date(2_000L)
        val pending = randomMessage(user = userA, createdAt = null, createdLocallyAt = localA)
        val received = randomMessage(user = userB, createdAt = null, createdLocallyAt = localB)

        val sorted = listOf(received, pending).sortedWith(MessageSortComparator)

        sorted[0].createdLocallyAt shouldBeEqualTo localA
        sorted[1].createdLocallyAt shouldBeEqualTo localB
    }

    @Test
    fun `messages with neither timestamp sort to front`() {
        val user = randomUser(id = "user")
        val withTimestamp = randomMessage(user = user, createdAt = Date(1_000L), createdLocallyAt = null)
        val noTimestamp = randomMessage(user = user, createdAt = null, createdLocallyAt = null)

        val sorted = listOf(withTimestamp, noTimestamp).sortedWith(MessageSortComparator)

        sorted[0].createdAt shouldBeEqualTo null
        sorted[1].createdAt shouldBeEqualTo Date(1_000L)
    }
}
