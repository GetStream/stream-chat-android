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

import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

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
    fun `should generate the same member-based channelId whatever the members order`() {
        generateChannelIdIfNeeded("", listOf("user2", "user1")) shouldBeEqualTo "!members-user1, user2"
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

    @Test
    fun `a channel named after its members in any order is a local placeholder`() {
        placeholder(id = "!members-user1, user2", "user2", "user1").isLocalPlaceholder() shouldBeEqualTo true
        placeholder(id = "!members-user2, user1", "user1", "user2").isLocalPlaceholder() shouldBeEqualTo true
    }

    @Test
    fun `a server distinct channel or one named after other members is not a local placeholder`() {
        placeholder(id = "!members-Zq3kP9xT2", "user1", "user2").isLocalPlaceholder() shouldBeEqualTo false
        placeholder(id = "!members-user1, user3", "user1", "user2").isLocalPlaceholder() shouldBeEqualTo false
        placeholder(id = "general", "user1", "user2").isLocalPlaceholder() shouldBeEqualTo false
    }

    @Test
    fun `a cid named after several members is a placeholder cid and a server distinct cid is not`() {
        isMultiMemberPlaceholderCid("messaging:!members-a, b") shouldBeEqualTo true
        isMultiMemberPlaceholderCid("messaging:!members-Zq3kP9xT2") shouldBeEqualTo false
        isMultiMemberPlaceholderCid("messaging:general") shouldBeEqualTo false
    }

    private fun placeholder(id: String, vararg memberIds: String) =
        randomChannel(id = id, members = memberIds.map { randomMember(user = randomUser(id = it)) })

    @Test
    fun `Given regular message with newer date, When calculateNewLastMessageAt is called, Then returns message date`() {
        // given
        val currentDate = Date(100L)
        val messageDate = Date(200L)
        val message = randomMessage(
            createdAt = messageDate,
            createdLocallyAt = null,
            type = MessageType.REGULAR,
            shadowed = false,
            parentId = null,
        )

        // when
        val result = calculateNewLastMessageAt(
            message = message,
            currentLastMessageAt = currentDate,
            skipLastMsgUpdateForSystemMsgs = false,
        )

        // then
        assertEquals(messageDate, result)
    }

    @Test
    fun `Given regular message with older date, When calculateNewLastMessageAt is called, Then returns current date`() {
        // given
        val currentDate = Date(200L)
        val messageDate = Date(100L)
        val message = randomMessage(
            createdAt = messageDate,
            createdLocallyAt = null,
            type = MessageType.REGULAR,
            shadowed = false,
            parentId = null,
        )

        // when
        val result = calculateNewLastMessageAt(
            message = message,
            currentLastMessageAt = currentDate,
            skipLastMsgUpdateForSystemMsgs = false,
        )

        // then
        assertEquals(currentDate, result)
    }

    @Test
    fun `Given null current date and message with date, When calculateNewLastMessageAt is called, Then returns message date`() {
        // given
        val messageDate = Date(200L)
        val message = randomMessage(
            createdAt = messageDate,
            createdLocallyAt = null,
            type = MessageType.REGULAR,
            shadowed = false,
            parentId = null,
        )

        // when
        val result = calculateNewLastMessageAt(
            message = message,
            currentLastMessageAt = null,
            skipLastMsgUpdateForSystemMsgs = false,
        )

        // then
        assertEquals(messageDate, result)
    }

    @Test
    fun `Given message with createdLocallyAt, When calculateNewLastMessageAt is called, Then uses createdLocallyAt over createdAt`() {
        // given
        val currentDate = Date(100L)
        val createdAt = Date(150L)
        val createdLocallyAt = Date(200L)
        val message = randomMessage(
            createdAt = createdAt,
            createdLocallyAt = createdLocallyAt,
            type = MessageType.REGULAR,
            shadowed = false,
            parentId = null,
        )

        // when
        val result = calculateNewLastMessageAt(
            message = message,
            currentLastMessageAt = currentDate,
            skipLastMsgUpdateForSystemMsgs = false,
        )

        // then
        assertEquals(createdLocallyAt, result)
    }

    @Test
    fun `Given shadowed message, When calculateNewLastMessageAt is called, Then returns current date unchanged`() {
        // given
        val currentDate = Date(100L)
        val messageDate = Date(200L)
        val message = randomMessage(
            createdAt = messageDate,
            createdLocallyAt = null,
            type = MessageType.REGULAR,
            shadowed = true,
            parentId = null,
        )

        // when
        val result = calculateNewLastMessageAt(
            message = message,
            currentLastMessageAt = currentDate,
            skipLastMsgUpdateForSystemMsgs = false,
        )

        // then
        assertEquals(currentDate, result)
    }

    @Test
    fun `Given system message with skip enabled, When calculateNewLastMessageAt is called, Then returns current date unchanged`() {
        // given
        val currentDate = Date(100L)
        val messageDate = Date(200L)
        val message = randomMessage(
            createdAt = messageDate,
            createdLocallyAt = null,
            type = MessageType.SYSTEM,
            shadowed = false,
            parentId = null,
        )

        // when
        val result = calculateNewLastMessageAt(
            message = message,
            currentLastMessageAt = currentDate,
            skipLastMsgUpdateForSystemMsgs = true,
        )

        // then
        assertEquals(currentDate, result)
    }

    @Test
    fun `Given system message with skip disabled, When calculateNewLastMessageAt is called, Then returns message date`() {
        // given
        val currentDate = Date(100L)
        val messageDate = Date(200L)
        val message = randomMessage(
            createdAt = messageDate,
            createdLocallyAt = null,
            type = MessageType.SYSTEM,
            shadowed = false,
            parentId = null,
        )

        // when
        val result = calculateNewLastMessageAt(
            message = message,
            currentLastMessageAt = currentDate,
            skipLastMsgUpdateForSystemMsgs = false,
        )

        // then
        assertEquals(messageDate, result)
    }

    @Test
    fun `Given thread reply not shown in channel, When calculateNewLastMessageAt is called, Then returns current date unchanged`() {
        // given
        val currentDate = Date(100L)
        val messageDate = Date(200L)
        val message = randomMessage(
            createdAt = messageDate,
            createdLocallyAt = null,
            type = MessageType.REGULAR,
            shadowed = false,
            parentId = "parent-id",
            showInChannel = false,
        )

        // when
        val result = calculateNewLastMessageAt(
            message = message,
            currentLastMessageAt = currentDate,
            skipLastMsgUpdateForSystemMsgs = false,
        )

        // then
        assertEquals(currentDate, result)
    }

    @Test
    fun `Given thread reply shown in channel, When calculateNewLastMessageAt is called, Then returns message date`() {
        // given
        val currentDate = Date(100L)
        val messageDate = Date(200L)
        val message = randomMessage(
            createdAt = messageDate,
            createdLocallyAt = null,
            type = MessageType.REGULAR,
            shadowed = false,
            parentId = "parent-id",
            showInChannel = true,
        )

        // when
        val result = calculateNewLastMessageAt(
            message = message,
            currentLastMessageAt = currentDate,
            skipLastMsgUpdateForSystemMsgs = false,
        )

        // then
        assertEquals(messageDate, result)
    }

    @Test
    fun `Given message with no date, When calculateNewLastMessageAt is called, Then returns current date unchanged`() {
        // given
        val currentDate = Date(100L)
        val message = randomMessage(
            createdAt = null,
            createdLocallyAt = null,
            type = MessageType.REGULAR,
            shadowed = false,
            parentId = null,
        )

        // when
        val result = calculateNewLastMessageAt(
            message = message,
            currentLastMessageAt = currentDate,
            skipLastMsgUpdateForSystemMsgs = false,
        )

        // then
        assertEquals(currentDate, result)
    }

    @Test
    fun `Given message with no date and null current date, When calculateNewLastMessageAt is called, Then returns null`() {
        // given
        val message = randomMessage(
            createdAt = null,
            createdLocallyAt = null,
            type = MessageType.REGULAR,
            shadowed = false,
            parentId = null,
        )

        // when
        val result = calculateNewLastMessageAt(
            message = message,
            currentLastMessageAt = null,
            skipLastMsgUpdateForSystemMsgs = false,
        )

        // then
        assertEquals(null, result)
    }
}
