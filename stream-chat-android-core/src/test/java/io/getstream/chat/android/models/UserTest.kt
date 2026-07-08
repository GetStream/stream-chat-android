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

import io.getstream.chat.android.DeliveryReceipts
import io.getstream.chat.android.PrivacySettings
import io.getstream.chat.android.ReadReceipts
import io.getstream.chat.android.TypingIndicators
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomChannelMute
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomDevice
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomLong
import io.getstream.chat.android.randomMute
import io.getstream.chat.android.randomPrivacySettings
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class UserTest {

    @Test
    @Suppress("LongMethod")
    fun `builder should set every field`() {
        val expected = User(
            id = randomString(),
            role = randomString(),
            name = randomString(),
            image = randomString(),
            invisible = randomBoolean(),
            privacySettings = randomPrivacySettings(),
            language = randomString(),
            banned = randomBoolean(),
            devices = listOf(randomDevice()),
            online = randomBoolean(),
            createdAt = randomDate(),
            updatedAt = randomDate(),
            lastActive = randomDate(),
            totalUnreadCount = randomInt(),
            unreadChannels = randomInt(),
            unreadThreads = randomInt(),
            mutes = listOf(randomMute()),
            teams = listOf(randomString()),
            teamsRole = mapOf(randomString() to randomString()),
            channelMutes = listOf(randomChannelMute()),
            blockedUserIds = listOf(randomString()),
            avgResponseTime = randomLong(),
            pushPreference = PushPreference(level = PushPreferenceLevel.all, disabledUntil = randomDate()),
            extraData = mapOf(randomString() to randomString()),
            deactivatedAt = randomDate(),
        )

        val built = User.Builder()
            .withId(expected.id)
            .withRole(expected.role)
            .withName(expected.name)
            .withImage(expected.image)
            .withInvisible(expected.invisible)
            .withPrivacySettings(expected.privacySettings)
            .withLanguage(expected.language)
            .withBanned(expected.banned)
            .withDevices(expected.devices)
            .withOnline(expected.online)
            .withCreatedAt(expected.createdAt)
            .withUpdatedAt(expected.updatedAt)
            .withLastActive(expected.lastActive)
            .withTotalUnreadCount(expected.totalUnreadCount)
            .withUnreadChannels(expected.unreadChannels)
            .withUnreadThreads(expected.unreadThreads)
            .withMutes(expected.mutes)
            .withTeams(expected.teams)
            .withTeamsRole(expected.teamsRole)
            .withChannelMutes(expected.channelMutes)
            .withBlockedUserIds(expected.blockedUserIds)
            .withAvgResponseTime(requireNotNull(expected.avgResponseTime))
            .withPushPreference(expected.pushPreference)
            .withExtraData(expected.extraData)
            .withDeactivatedAt(expected.deactivatedAt)
            .build()

        assertEquals(expected, built)
    }

    @Test
    fun `builder copy constructor should copy every field`() {
        val user = randomUser(
            privacySettings = randomPrivacySettings(),
            devices = listOf(randomDevice()),
            mutes = listOf(randomMute()),
            channelMutes = listOf(randomChannelMute()),
            blockedUserIds = listOf(randomString()),
        ).copy(
            unreadThreads = randomInt(),
            avgResponseTime = randomLong(),
            pushPreference = PushPreference(level = PushPreferenceLevel.all, disabledUntil = randomDate()),
        )

        val built = User.Builder(user).build()

        assertEquals(user, built)
    }

    @Test
    fun `isBanned should be true only when banned is true`() {
        assertTrue(randomUser(banned = true).isBanned)
        assertFalse(randomUser(banned = false).isBanned)
        assertFalse(randomUser().copy(banned = null).isBanned)
    }

    @Test
    fun `isInvisible should be true only when invisible is true`() {
        assertTrue(randomUser(invisible = true).isInvisible)
        assertFalse(randomUser(invisible = false).isInvisible)
        assertFalse(randomUser().copy(invisible = null).isInvisible)
    }

    @Test
    fun `isTypingIndicatorsEnabled should default to true when no settings are present`() {
        assertTrue(randomUser(privacySettings = null).isTypingIndicatorsEnabled)
        assertTrue(randomUser(privacySettings = PrivacySettings(typingIndicators = null)).isTypingIndicatorsEnabled)
    }

    @Test
    fun `isTypingIndicatorsEnabled should reflect the typing indicators setting`() {
        val enabled = PrivacySettings(typingIndicators = TypingIndicators(enabled = true))
        val disabled = PrivacySettings(typingIndicators = TypingIndicators(enabled = false))
        assertTrue(randomUser(privacySettings = enabled).isTypingIndicatorsEnabled)
        assertFalse(randomUser(privacySettings = disabled).isTypingIndicatorsEnabled)
    }

    @Test
    fun `isReadReceiptsEnabled should default to true when no settings are present`() {
        assertTrue(randomUser(privacySettings = null).isReadReceiptsEnabled)
        assertTrue(randomUser(privacySettings = PrivacySettings(readReceipts = null)).isReadReceiptsEnabled)
    }

    @Test
    fun `isReadReceiptsEnabled should reflect the read receipts setting`() {
        val enabled = PrivacySettings(readReceipts = ReadReceipts(enabled = true))
        val disabled = PrivacySettings(readReceipts = ReadReceipts(enabled = false))
        assertTrue(randomUser(privacySettings = enabled).isReadReceiptsEnabled)
        assertFalse(randomUser(privacySettings = disabled).isReadReceiptsEnabled)
    }

    @Test
    fun `isDeliveryReceiptsEnabled should default to true when no settings are present`() {
        assertTrue(randomUser(privacySettings = null).isDeliveryReceiptsEnabled)
        assertTrue(randomUser(privacySettings = PrivacySettings(deliveryReceipts = null)).isDeliveryReceiptsEnabled)
    }

    @Test
    fun `isDeliveryReceiptsEnabled should reflect the delivery receipts setting`() {
        val enabled = PrivacySettings(deliveryReceipts = DeliveryReceipts(enabled = true))
        val disabled = PrivacySettings(deliveryReceipts = DeliveryReceipts(enabled = false))
        assertTrue(randomUser(privacySettings = enabled).isDeliveryReceiptsEnabled)
        assertFalse(randomUser(privacySettings = disabled).isDeliveryReceiptsEnabled)
    }

    @Test
    fun `getComparableField should return string fields`() {
        val user = randomUser()
        assertEquals(user.id, user.getComparableField("id"))
        assertEquals(user.role, user.getComparableField("role"))
        assertEquals(user.name, user.getComparableField("name"))
        assertEquals(user.image, user.getComparableField("image"))
        assertEquals(user.language, user.getComparableField("language"))
    }

    @Test
    fun `getComparableField should return boolean fields`() {
        val user = randomUser()
        assertEquals(user.invisible, user.getComparableField("invisible"))
        assertEquals(user.banned, user.getComparableField("banned"))
        assertEquals(user.online, user.getComparableField("online"))
    }

    @Test
    fun `getComparableField should return count fields for snake_case and camelCase field names`() {
        val user = randomUser().copy(unreadThreads = randomInt())
        assertEquals(user.totalUnreadCount, user.getComparableField("total_unread_count"))
        assertEquals(user.totalUnreadCount, user.getComparableField("totalUnreadCount"))
        assertEquals(user.unreadChannels, user.getComparableField("unread_channels"))
        assertEquals(user.unreadChannels, user.getComparableField("unreadChannels"))
        assertEquals(user.unreadThreads, user.getComparableField("unread_threads"))
        assertEquals(user.unreadThreads, user.getComparableField("unreadThreads"))
    }

    @Test
    fun `getComparableField should return date fields for snake_case and camelCase field names`() {
        val user = randomUser(
            createdAt = randomDate(),
            deactivatedAt = randomDate(),
            updatedAt = randomDate(),
            lastActive = randomDate(),
        )
        assertEquals(user.createdAt, user.getComparableField("created_at"))
        assertEquals(user.createdAt, user.getComparableField("createdAt"))
        assertEquals(user.deactivatedAt, user.getComparableField("deactivated_at"))
        assertEquals(user.deactivatedAt, user.getComparableField("deactivatedAt"))
        assertEquals(user.updatedAt, user.getComparableField("updated_at"))
        assertEquals(user.updatedAt, user.getComparableField("updatedAt"))
        assertEquals(user.lastActive, user.getComparableField("last_active"))
        assertEquals(user.lastActive, user.getComparableField("lastActive"))
    }

    @Test
    fun `getComparableField should return avgResponseTime for snake_case and camelCase field names`() {
        val user = randomUser().copy(avgResponseTime = randomLong())
        assertEquals(user.avgResponseTime, user.getComparableField("avg_response_time"))
        assertEquals(user.avgResponseTime, user.getComparableField("avgResponseTime"))
    }

    @Test
    fun `getComparableField should return extraData value for custom field`() {
        val user = randomUser(extraData = mutableMapOf("customField" to "customValue"))
        assertEquals("customValue", user.getComparableField("customField"))
    }

    @Test
    fun `getComparableField should return null for unknown field`() {
        val user = randomUser(extraData = mutableMapOf())
        assertNull(user.getComparableField("unknownField"))
    }
}
