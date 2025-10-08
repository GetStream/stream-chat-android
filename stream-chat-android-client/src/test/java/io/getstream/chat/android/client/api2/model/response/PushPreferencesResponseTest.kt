/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.api2.model.response

import io.getstream.chat.android.client.Mother
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.Date

internal class PushPreferencesResponseTest {

    @Test
    fun `getUserPreference should return user preference when user exists`() {
        // Given
        val userId = "user123"
        val pushPreference = Mother.randomDownstreamPushPreferenceDto(
            chatLevel = "all",
            disabledUntil = Date(),
        )
        val response = PushPreferencesResponse(
            user_channel_preferences = emptyMap(),
            user_preferences = mapOf(userId to pushPreference),
        )

        // When
        val result = response.getUserPreference(userId)

        // Then
        assertEquals(pushPreference, result)
    }

    @Test
    fun `getUserPreference should return null when user does not exist`() {
        // Given
        val userId = "nonexistent_user"
        val response = PushPreferencesResponse(
            user_channel_preferences = emptyMap(),
            user_preferences = mapOf("other_user" to Mother.randomDownstreamPushPreferenceDto()),
        )

        // When
        val result = response.getUserPreference(userId)

        // Then
        assertNull(result)
    }

    @Test
    fun `getUserPreference should return null when user preference is explicitly null`() {
        // Given
        val userId = "user123"
        val response = PushPreferencesResponse(
            user_channel_preferences = emptyMap(),
            user_preferences = mapOf(userId to null),
        )

        // When
        val result = response.getUserPreference(userId)

        // Then
        assertNull(result)
    }

    @Test
    fun `getUserPreference should return null when user_preferences map is empty`() {
        // Given
        val userId = "user123"
        val response = PushPreferencesResponse(
            user_channel_preferences = emptyMap(),
            user_preferences = emptyMap(),
        )

        // When
        val result = response.getUserPreference(userId)

        // Then
        assertNull(result)
    }

    @Test
    fun `getUserChannelPreference should return channel preference when user and channel exist`() {
        // Given
        val userId = "user123"
        val channelId = "messaging:channel456"
        val pushPreference = Mother.randomDownstreamPushPreferenceDto(
            chatLevel = "mentions",
            disabledUntil = null,
        )
        val response = PushPreferencesResponse(
            user_channel_preferences = mapOf(
                userId to mapOf(channelId to pushPreference),
            ),
            user_preferences = emptyMap(),
        )

        // When
        val result = response.getUserChannelPreference(userId, channelId)

        // Then
        assertEquals(pushPreference, result)
    }

    @Test
    fun `getUserChannelPreference should return null when user does not exist`() {
        // Given
        val userId = "nonexistent_user"
        val channelId = "messaging:channel456"
        val response = PushPreferencesResponse(
            user_channel_preferences = mapOf(
                "other_user" to mapOf(channelId to Mother.randomDownstreamPushPreferenceDto()),
            ),
            user_preferences = emptyMap(),
        )

        // When
        val result = response.getUserChannelPreference(userId, channelId)

        // Then
        assertNull(result)
    }

    @Test
    fun `getUserChannelPreference should return null when user exists but channel does not exist`() {
        // Given
        val userId = "user123"
        val channelId = "messaging:nonexistent_channel"
        val response = PushPreferencesResponse(
            user_channel_preferences = mapOf(
                userId to mapOf("messaging:other_channel" to Mother.randomDownstreamPushPreferenceDto()),
            ),
            user_preferences = emptyMap(),
        )

        // When
        val result = response.getUserChannelPreference(userId, channelId)

        // Then
        assertNull(result)
    }

    @Test
    fun `getUserChannelPreference should return null when user has empty channel preferences`() {
        // Given
        val userId = "user123"
        val channelId = "messaging:channel456"
        val response = PushPreferencesResponse(
            user_channel_preferences = mapOf(userId to emptyMap()),
            user_preferences = emptyMap(),
        )

        // When
        val result = response.getUserChannelPreference(userId, channelId)

        // Then
        assertNull(result)
    }

    @Test
    fun `getUserChannelPreference should return null when user_channel_preferences map is empty`() {
        // Given
        val userId = "user123"
        val channelId = "messaging:channel456"
        val response = PushPreferencesResponse(
            user_channel_preferences = emptyMap(),
            user_preferences = emptyMap(),
        )

        // When
        val result = response.getUserChannelPreference(userId, channelId)

        // Then
        assertNull(result)
    }

    @Test
    fun `getUserChannelPreference should handle multiple users and channels correctly`() {
        // Given
        val user1 = "user1"
        val user2 = "user2"
        val channel1 = "messaging:channel1"
        val channel2 = "messaging:channel2"
        val preference1 = Mother.randomDownstreamPushPreferenceDto(chatLevel = "all")
        val preference2 = Mother.randomDownstreamPushPreferenceDto(chatLevel = "mentions")
        val preference3 = Mother.randomDownstreamPushPreferenceDto(chatLevel = "none")

        val response = PushPreferencesResponse(
            user_channel_preferences = mapOf(
                user1 to mapOf(
                    channel1 to preference1,
                    channel2 to preference2,
                ),
                user2 to mapOf(
                    channel1 to preference3,
                ),
            ),
            user_preferences = emptyMap(),
        )

        // When & Then
        assertEquals(preference1, response.getUserChannelPreference(user1, channel1))
        assertEquals(preference2, response.getUserChannelPreference(user1, channel2))
        assertEquals(preference3, response.getUserChannelPreference(user2, channel1))
        assertNull(response.getUserChannelPreference(user2, channel2))
        assertNull(response.getUserChannelPreference("user3", channel1))
    }

    @Test
    fun `getUserPreference should handle multiple users correctly`() {
        // Given
        val user1 = "user1"
        val user2 = "user2"
        val preference1 = Mother.randomDownstreamPushPreferenceDto(chatLevel = "all")
        val preference2 = Mother.randomDownstreamPushPreferenceDto(chatLevel = "mentions")

        val response = PushPreferencesResponse(
            user_channel_preferences = emptyMap(),
            user_preferences = mapOf(
                user1 to preference1,
                user2 to preference2,
            ),
        )

        // When & Then
        assertEquals(preference1, response.getUserPreference(user1))
        assertEquals(preference2, response.getUserPreference(user2))
        assertNull(response.getUserPreference("user3"))
    }
}
