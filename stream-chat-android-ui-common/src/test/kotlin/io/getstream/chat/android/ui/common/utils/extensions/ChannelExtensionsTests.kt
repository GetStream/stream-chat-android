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

package io.getstream.chat.android.ui.common.utils.extensions

import android.content.Context
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.User
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.ui.common.R
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

internal class ChannelExtensionsTests {

    private val context: Context = mock()
    private val fallbackResource: Int = positiveRandomInt()
    private val fallbackText: String = randomString()
    private val currentUser = randomUser()

    @BeforeEach
    fun setup() {
        whenever(context.getString(fallbackResource)) doReturn fallbackText
        whenever(
            context.getString(
                eq(R.string.stream_ui_channel_list_untitled_channel_plus_more),
                anyString(),
                anyInt(),
            ),
        ) doAnswer {
            val users = it.arguments[1] as String
            val count = it.arguments[2] as Int
            "$users +$count more"
        }
    }

    @Test
    fun `Should return silent regular message as preview when it is the latest`() {
        // Given
        val user = randomUser()
        val olderMessage = randomMessage(
            text = "Older message",
            type = MessageType.REGULAR,
            silent = false,
            createdAt = Date(1000L),
            createdLocallyAt = null,
            deletedAt = null,
            deletedForMe = false,
            user = user,
            shadowed = false,
        )
        val latestSilentMessage = randomMessage(
            text = "Latest silent message",
            type = MessageType.REGULAR,
            silent = true,
            createdAt = Date(2000L),
            createdLocallyAt = null,
            deletedAt = null,
            deletedForMe = false,
            user = user,
            shadowed = false,
        )
        val channel = randomChannel(isInsideSearch = false, messages = listOf(olderMessage, latestSilentMessage))

        // When
        val previewMessage = channel.getPreviewMessage(user)

        // Then
        Assertions.assertEquals(latestSilentMessage, previewMessage)
    }

    @Test
    fun `Should return non-silent regular message as preview when it is the latest`() {
        // Given
        val user = randomUser()
        val olderSilentMessage = randomMessage(
            text = "Older silent message",
            type = MessageType.REGULAR,
            silent = true,
            createdAt = Date(1000L),
            createdLocallyAt = null,
            deletedAt = null,
            deletedForMe = false,
            user = user,
            shadowed = false,
        )
        val latestNonSilentMessage = randomMessage(
            text = "Latest non-silent message",
            type = MessageType.REGULAR,
            silent = false,
            createdAt = Date(2000L),
            createdLocallyAt = null,
            deletedAt = null,
            deletedForMe = false,
            user = user,
            shadowed = false,
        )
        val channel = randomChannel(
            isInsideSearch = false,
            messages = listOf(olderSilentMessage, latestNonSilentMessage),
        )

        // When
        val previewMessage = channel.getPreviewMessage(user)

        // Then
        Assertions.assertEquals(latestNonSilentMessage, previewMessage)
    }

    @Test
    fun `Should return system message as preview when it is the latest`() {
        // Given
        val user = randomUser()
        val olderRegularMessage = randomMessage(
            text = "Older regular message",
            type = MessageType.REGULAR,
            silent = false,
            createdAt = Date(1000L),
            createdLocallyAt = null,
            deletedAt = null,
            deletedForMe = false,
            user = user,
            shadowed = false,
        )
        val latestSystemMessage = randomMessage(
            text = "User joined the channel",
            type = MessageType.SYSTEM,
            silent = false,
            createdAt = Date(2000L),
            createdLocallyAt = null,
            deletedAt = null,
            deletedForMe = false,
            user = user,
            shadowed = false,
        )
        val channel = randomChannel(
            isInsideSearch = false,
            messages = listOf(olderRegularMessage, latestSystemMessage),
        )

        // When
        val previewMessage = channel.getPreviewMessage(user)

        // Then
        Assertions.assertEquals(latestSystemMessage, previewMessage)
    }

    @Test
    fun `Should ignore deleted messages for preview`() {
        // Given
        val user = randomUser()
        val validMessage = randomMessage(
            text = "Valid message",
            type = MessageType.REGULAR,
            createdAt = Date(1000L),
            createdLocallyAt = null,
            deletedAt = null,
            deletedForMe = false,
            user = user,
            shadowed = false,
        )
        val deletedSilentMessage = randomMessage(
            text = "Deleted silent message",
            type = MessageType.REGULAR,
            createdAt = Date(2000L),
            createdLocallyAt = null,
            deletedAt = Date(2500L),
            user = user,
            shadowed = false,
        )
        val channel = randomChannel(
            isInsideSearch = false,
            messages = listOf(validMessage, deletedSilentMessage),
        )

        // When
        val previewMessage = channel.getPreviewMessage(user)

        // Then
        Assertions.assertEquals(validMessage, previewMessage)
    }

    @Test
    fun `Should return null when no valid messages exist`() {
        // Given
        val user = randomUser()
        val deletedMessage = randomMessage(
            text = "Deleted message",
            type = MessageType.REGULAR,
            silent = false,
            createdAt = Date(1000L),
            createdLocallyAt = null,
            deletedAt = Date(1500L),
            user = user,
            shadowed = false,
        )
        val ephemeralMessage = randomMessage(
            text = "Ephemeral message",
            type = MessageType.EPHEMERAL,
            silent = false,
            createdAt = Date(2000L),
            createdLocallyAt = null,
            deletedAt = null,
            user = user,
            shadowed = false,
        )
        val channel = randomChannel(
            isInsideSearch = false,
            messages = listOf(deletedMessage, ephemeralMessage),
        )

        // When
        val previewMessage = channel.getPreviewMessage(user)

        // Then
        Assertions.assertNull(previewMessage)
    }

    @Test
    fun `Should return channel name when name is set`() {
        // Given
        val channel = randomChannel()
        val maxMembers = positiveRandomInt()

        // When
        val displayName = channel.getDisplayName(context, currentUser, fallbackResource, maxMembers)

        // Then
        displayName `should be equal to` channel.name
    }

    @Test
    fun `Should return member name when channel has no name and single other member`() {
        // Given
        val otherUser = randomUser()
        val channel = randomChannel(name = "", members = listOf(randomMember(user = otherUser)))
        val maxMembers = positiveRandomInt()

        // When
        val displayName = channel.getDisplayName(context, currentUser, fallbackResource, maxMembers)

        // Then
        displayName `should be equal to` otherUser.name
    }

    @Test
    fun `Should return comma-separated member names when channel has no name and members within limit`() {
        // Given
        val members = List(positiveRandomInt(4)) { randomUser() }
        val channel = randomChannel(name = "", members = members.map { user -> randomMember(user = user) })
        val maxMembers = 4
        val expectedName = members.sortedBy(User::name).joinToString(", ") { user -> user.name }

        // When
        val displayName = channel.getDisplayName(context, currentUser, fallbackResource, maxMembers)

        // Then
        displayName `should be equal to` expectedName
    }

    @Test
    fun `Should return truncated member names with more indicator when channel has no name and members exceed limit`() {
        // Given
        val members = List(5) { randomUser() }
        val maxMembers = positiveRandomInt(4)
        val channel = randomChannel(name = "", members = members.map { user -> randomMember(user = user) })
        val expectedName = "${
            members.sortedBy(User::name)
                .take(maxMembers)
                .joinToString(separator = ", ") { user -> user.name }
        } +${members.size - maxMembers} more"

        // When
        val displayName = channel.getDisplayName(context, currentUser, fallbackResource, maxMembers)

        // Then
        displayName `should be equal to` expectedName
    }

    @Test
    fun `Should return current user name when channel has no name and only current user is member`() {
        // Given
        val channel = randomChannel(
            name = "",
            members = listOf(randomMember(user = currentUser)),
        )
        val maxMembers = randomInt()

        // When
        val displayName = channel.getDisplayName(context, currentUser, fallbackResource, maxMembers)

        // Then
        displayName `should be equal to` currentUser.name
    }

    @Test
    fun `Should return fallback text when channel has no name and no members`() {
        // Given
        val channel = randomChannel(
            name = "",
            members = emptyList(),
        )
        val maxMembers = positiveRandomInt()

        // When
        val displayName = channel.getDisplayName(context, currentUser, fallbackResource, maxMembers)

        // Then
        displayName `should be equal to` fallbackText
    }
}
