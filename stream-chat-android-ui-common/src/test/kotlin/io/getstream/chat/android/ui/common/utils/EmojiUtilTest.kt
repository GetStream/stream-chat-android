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

package io.getstream.chat.android.ui.common.utils

import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.Date

internal class EmojiUtilTest {

    @Test
    fun `isEmojiOnly should return true for single emoji`() {
        // Given
        val message = randomMessage(
            text = "😀",
            deletedAt = null,
        )

        // When
        val result = EmojiUtil.isEmojiOnly(message)

        // Then
        Assertions.assertTrue(result)
    }

    @Test
    fun `isEmojiOnly should return true for multiple emojis`() {
        // Given
        val message = randomMessage(
            text = "😀😃😄",
            deletedAt = null,
        )

        // When
        val result = EmojiUtil.isEmojiOnly(message)

        // Then
        Assertions.assertTrue(result)
    }

    @Test
    fun `isEmojiOnly should return false for text with emoji`() {
        // Given
        val message = randomMessage(
            text = "Hello 😀",
            deletedAt = null,
        )

        // When
        val result = EmojiUtil.isEmojiOnly(message)

        // Then
        Assertions.assertFalse(result)
    }

    @Test
    fun `isEmojiOnly should return false for text only`() {
        // Given
        val message = randomMessage(
            text = randomString(),
            deletedAt = null,
        )

        // When
        val result = EmojiUtil.isEmojiOnly(message)

        // Then
        Assertions.assertFalse(result)
    }

    @Test
    fun `isEmojiOnly should return false for deleted message with emoji`() {
        // Given
        val message = randomMessage(
            text = "😀",
            deletedAt = Date(),
        )

        // When
        val result = EmojiUtil.isEmojiOnly(message)

        // Then
        Assertions.assertFalse(result)
    }

    @Test
    fun `isSingleEmoji should return true for single emoji`() {
        // Given
        val message = randomMessage(
            text = "😀",
            deletedAt = null,
        )

        // When
        val result = EmojiUtil.isSingleEmoji(message)

        // Then
        Assertions.assertTrue(result)
    }

    @Test
    fun `isSingleEmoji should return false for multiple emojis`() {
        // Given
        val message = randomMessage(
            text = "😀😃",
            deletedAt = null,
        )

        // When
        val result = EmojiUtil.isSingleEmoji(message)

        // Then
        Assertions.assertFalse(result)
    }

    @Test
    fun `isSingleEmoji should return false for text with single emoji`() {
        // Given
        val message = randomMessage(
            text = "Hello 😀",
            deletedAt = null,
        )

        // When
        val result = EmojiUtil.isSingleEmoji(message)

        // Then
        Assertions.assertFalse(result)
    }

    @Test
    fun `isSingleEmoji should return false for text only`() {
        // Given
        val message = randomMessage(
            text = randomString(),
            deletedAt = null,
        )

        // When
        val result = EmojiUtil.isSingleEmoji(message)

        // Then
        Assertions.assertFalse(result)
    }

    @Test
    fun `isSingleEmoji should return false for deleted message with single emoji`() {
        // Given
        val message = randomMessage(
            text = "😀",
            deletedAt = Date(),
        )

        // When
        val result = EmojiUtil.isSingleEmoji(message)

        // Then
        Assertions.assertFalse(result)
    }

    @Test
    fun `isSingleEmoji(String) should return true for single emoji`() {
        Assertions.assertTrue(EmojiUtil.isSingleEmoji("😀"))
    }

    @Test
    fun `isSingleEmoji(String) should return false for multiple emojis`() {
        Assertions.assertFalse(EmojiUtil.isSingleEmoji("😀😃"))
    }

    @Test
    fun `isSingleEmoji(String) should return false for plain text`() {
        Assertions.assertFalse(EmojiUtil.isSingleEmoji("like"))
    }

    @Test
    fun `isSingleEmoji(String) should return false for empty string`() {
        Assertions.assertFalse(EmojiUtil.isSingleEmoji(""))
    }

    @Test
    fun `isSingleEmoji(String) should return false for text with emoji`() {
        Assertions.assertFalse(EmojiUtil.isSingleEmoji("Hello 😀"))
    }

    @Test
    fun `getEmojiCount should return 0 for text without emoji`() {
        // Given
        val message = randomMessage(
            text = randomString(),
            deletedAt = null,
        )

        // When
        val count = EmojiUtil.getEmojiCount(message)

        // Then
        Assertions.assertEquals(0, count)
    }

    @Test
    fun `getEmojiCount should return 0 for empty text`() {
        // Given
        val message = randomMessage(
            text = "",
            deletedAt = null,
        )

        // When
        val count = EmojiUtil.getEmojiCount(message)

        // Then
        Assertions.assertEquals(0, count)
    }

    @Test
    fun `getEmojiCount should return 1 for single emoji`() {
        // Given
        val message = randomMessage(
            text = "😀",
            deletedAt = null,
        )

        // When
        val count = EmojiUtil.getEmojiCount(message)

        // Then
        Assertions.assertEquals(1, count)
    }

    @Test
    fun `getEmojiCount should return count for multiple emojis`() {
        // Given
        val message = randomMessage(
            text = "😀😃",
            deletedAt = null,
        )

        // When
        val count = EmojiUtil.getEmojiCount(message)

        // Then
        Assertions.assertEquals(2, count)
    }

    @Test
    fun `getEmojiCount should count emojis in text with words`() {
        // Given
        val message = randomMessage(
            text = "Hello 😀 world 😃!",
            deletedAt = null,
        )

        // When
        val count = EmojiUtil.getEmojiCount(message)

        // Then
        Assertions.assertEquals(2, count)
    }

    @Test
    fun `getEmojiCount should count emojis in message with deleted status`() {
        // Given
        val message = randomMessage(
            text = "😀😃😄",
            deletedAt = Date(),
        )

        // When
        val count = EmojiUtil.getEmojiCount(message)

        // Then
        Assertions.assertEquals(3, count)
    }
}
