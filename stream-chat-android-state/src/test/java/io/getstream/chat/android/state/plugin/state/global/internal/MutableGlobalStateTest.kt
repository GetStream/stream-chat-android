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

package io.getstream.chat.android.state.plugin.state.global.internal

import io.getstream.chat.android.randomLocation
import io.getstream.chat.android.randomLong
import io.getstream.chat.android.randomString
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Date

internal class MutableGlobalStateTest {

    private val userId = randomString()
    private val now = randomLong()
    private val sut = MutableGlobalState(userId = userId, now = { now })

    @Test
    fun `addLiveLocations should add locations sorted by messageId and remove expired ones`() = runTest {
        val currentUserLocation = randomLocation(
            userId = userId,
            messageId = "2",
            endAt = Date(now + 10000),
        )
        val expiredLocation = randomLocation(
            userId = userId,
            messageId = randomString(),
            endAt = Date(now - 10000),
        )
        val otherUserLocation = randomLocation(
            userId = randomString(),
            messageId = "1",
            endAt = Date(now + 10000),
        )

        sut.addLiveLocations(locations = listOf(currentUserLocation, expiredLocation, otherUserLocation))

        val result = sut.activeLiveLocations.first()
        assertEquals(2, result.size)
        assertEquals(otherUserLocation, result[0])
        assertEquals(currentUserLocation, result[1])
    }

    @Test
    fun `currentUserActiveLiveLocations should hold active live locations of the current user only`() = runTest {
        val currentUserLocation = randomLocation(
            userId = userId,
            messageId = randomString(),
            endAt = Date(now + 10000),
        )
        val otherUserLocation = randomLocation(
            userId = randomString(),
            messageId = randomString(),
            endAt = Date(now + 10000),
        )

        sut.addLiveLocations(locations = listOf(currentUserLocation, otherUserLocation))

        val result = sut.currentUserActiveLiveLocations.first()
        assertEquals(1, result.size)
        assertEquals(currentUserLocation, result[0])
    }

    @Test
    fun `addLiveLocations should replace location with same messageId for current user`() = runTest {
        val messageId = randomString()
        val location1 = randomLocation(
            userId = userId,
            messageId = messageId,
            endAt = Date(now + 10000),
        )
        val location2 = randomLocation(
            userId = userId,
            messageId = messageId,
            endAt = Date(now + 20000),
        )

        sut.addLiveLocations(locations = listOf(location1))
        sut.addLiveLocations(locations = listOf(location2))

        val result = sut.activeLiveLocations.first()
        assertEquals(1, result.size)
        assertEquals(location2, result[0])
    }

    @Test
    fun `removeExpiredLiveLocations should remove all expired locations`() = runTest {
        val validLocation = randomLocation(
            userId = userId,
            messageId = randomString(),
            endAt = Date(now + 10000),
        )
        val expiredLocation = randomLocation(
            userId = userId,
            messageId = randomString(),
            endAt = Date(now - 10000),
        )
        sut.addLiveLocations(listOf(validLocation, expiredLocation))

        sut.removeExpiredLiveLocations()

        val result = sut.activeLiveLocations.first()
        assertEquals(1, result.size)
        assertEquals(validLocation, result[0])
    }

    @Test
    fun `removeExpiredLiveLocations should keep all locations if none are expired`() = runTest {
        val location1 = randomLocation(
            userId = userId,
            messageId = "1",
            endAt = Date(now + 10000),
        )
        val location2 = randomLocation(
            userId = userId,
            messageId = "2",
            endAt = Date(now + 20000),
        )
        sut.addLiveLocations(listOf(location1, location2))

        sut.removeExpiredLiveLocations()

        val result = sut.activeLiveLocations.first()
        assertEquals(2, result.size)
        assertTrue(result.containsAll(listOf(location1, location2)))
    }

    @Test
    fun `removeExpiredLiveLocations should result in empty list if all locations are expired`() = runTest {
        val expiredLocation1 = randomLocation(
            userId = userId,
            messageId = randomString(),
            endAt = Date(now - 10000),
        )
        val expiredLocation2 = randomLocation(
            userId = userId,
            messageId = randomString(),
            endAt = Date(now - 20000),
        )
        sut.addLiveLocations(listOf(expiredLocation1, expiredLocation2))

        sut.removeExpiredLiveLocations()

        val result = sut.activeLiveLocations.first()
        assertTrue(result.isEmpty())
    }
}
