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

package io.getstream.chat.android.client.utils

import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannelMute
import io.getstream.chat.android.randomMute
import io.getstream.chat.android.randomPrivacySettings
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import java.util.Date

internal class UserUtilsTest {

    @Test
    fun `mergePartially should merge when that creation date is more up to date`() {
        // given
        val baseTime = System.currentTimeMillis()
        val thisUser = randomUser(
            createdAt = Date(baseTime),
            updatedAt = null,
            privacySettings = randomPrivacySettings(),
            extraData = mutableMapOf(randomString() to randomString()),
            mutes = List(10) { randomMute() },
        )
        val thatUser = randomUser(
            createdAt = Date(baseTime + 1000),
            updatedAt = null,
            privacySettings = randomPrivacySettings(),
            extraData = mutableMapOf(randomString() to randomString()),
            mutes = List(10) { randomMute() },
        )

        // when
        val result = thisUser.mergePartially(thatUser)

        // then
        result.isEqualTo(thatUser)
        assertEquals(thisUser.mutes, result.mutes)
    }

    @Test
    fun `mergePartially should merge when that update date is more up to date`() {
        // given
        val baseTime = System.currentTimeMillis()
        val thisUser = randomUser(
            createdAt = null,
            updatedAt = Date(baseTime),
            privacySettings = randomPrivacySettings(),
            extraData = mutableMapOf(randomString() to randomString()),
            mutes = List(10) { randomMute() },
        )
        val thatUser = randomUser(
            createdAt = null,
            updatedAt = Date(baseTime + 1000),
            privacySettings = randomPrivacySettings(),
            extraData = mutableMapOf(randomString() to randomString()),
            mutes = List(10) { randomMute() },
        )

        // when
        val result = thisUser.mergePartially(thatUser)

        // then
        result.isEqualTo(thatUser)
        assertEquals(thisUser.mutes, result.mutes)
    }

    @Test
    fun `mergePartially should keep the average response time when that has none`() {
        // given
        val thisUser = randomUser(updatedAt = Date(1_000)).copy(avgResponseTime = 42L)
        val thatUser = randomUser(updatedAt = Date(2_000)).copy(avgResponseTime = null)

        // when
        val result = thisUser.mergePartially(thatUser)

        // then
        assertEquals(42L, result.avgResponseTime)
    }

    @Test
    fun `mergePartially should copy the profile fields and keep the own-user fields`() {
        // given
        val thisUser = randomUser(
            updatedAt = Date(1_000),
            language = "en",
            teams = listOf("red"),
            mutes = List(3) { randomMute() },
            channelMutes = List(2) { randomChannelMute() },
        )
        val thatUser = randomUser(
            updatedAt = Date(2_000),
            language = "it",
            teams = listOf("blue"),
            mutes = emptyList(),
            channelMutes = listOf(randomChannelMute()),
        ).copy(
            teamsRole = mapOf("blue" to "admin"),
            deactivatedAt = Date(3_000),
            avgResponseTime = 7L,
        )

        // when
        val result = thisUser.mergePartially(thatUser)

        // then
        result.isEqualTo(thatUser)
        assertEquals(thisUser.mutes, result.mutes)
        assertEquals(thisUser.channelMutes, result.channelMutes)
        assertEquals(thisUser.totalUnreadCount, result.totalUnreadCount)
        assertEquals(thisUser.blockedUserIds, result.blockedUserIds)
    }

    @Test
    fun `mergePartially should return this when this creation date is more up to date`() {
        // given
        val baseTime = System.currentTimeMillis()
        val thisUser = randomUser(
            createdAt = Date(baseTime + 1000),
            updatedAt = null,
            privacySettings = randomPrivacySettings(),
            extraData = mutableMapOf(randomString() to randomString()),
            mutes = List(10) { randomMute() },
        )
        val thatUser = randomUser(
            createdAt = Date(baseTime),
            updatedAt = null,
            privacySettings = randomPrivacySettings(),
            extraData = mutableMapOf(randomString() to randomString()),
            mutes = List(10) { randomMute() },
        )

        // when
        val result = thisUser.mergePartially(thatUser)

        // then
        assertSame(thisUser, result)
    }

    @Test
    fun `mergePartially should return this when this update date is more up to date`() {
        // given
        val baseTime = System.currentTimeMillis()
        val thisUser = randomUser(
            createdAt = null,
            updatedAt = Date(baseTime + 1000),
            privacySettings = randomPrivacySettings(),
            extraData = mutableMapOf(randomString() to randomString()),
            mutes = List(10) { randomMute() },
        )
        val thatUser = randomUser(
            createdAt = null,
            updatedAt = Date(baseTime),
            privacySettings = randomPrivacySettings(),
            extraData = mutableMapOf(randomString() to randomString()),
            mutes = List(10) { randomMute() },
        )

        // when
        val result = thisUser.mergePartially(thatUser)

        // then
        assertSame(thisUser, result)
    }
}

private fun User.isEqualTo(that: User) {
    assertEquals(that.role, role)
    assertEquals(that.name, name)
    assertEquals(that.image, image)
    assertEquals(that.banned, banned)
    assertEquals(that.lastActive, lastActive)
    assertEquals(that.online, online)
    assertEquals(that.createdAt, createdAt)
    assertEquals(that.updatedAt, updatedAt)
    assertEquals(that.privacySettings, privacySettings)
    assertEquals(that.extraData, extraData)
    assertEquals(that.language, language)
    assertEquals(that.teams, teams)
    assertEquals(that.teamsRole, teamsRole)
    assertEquals(that.deactivatedAt, deactivatedAt)
    assertEquals(that.avgResponseTime, avgResponseTime)
}
