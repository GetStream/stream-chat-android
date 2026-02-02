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

import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.randomMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

internal class ChannelUtilsTest {

    // region calculateNewLastMessageAt tests

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

    // endregion
}
