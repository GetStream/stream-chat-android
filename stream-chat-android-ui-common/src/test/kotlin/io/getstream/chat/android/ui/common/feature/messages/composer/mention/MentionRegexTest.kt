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

package io.getstream.chat.android.ui.common.feature.messages.composer.mention

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class MentionRegexTest {

    @ParameterizedTest(name = "[{index}] display=\"{0}\" in text=\"{1}\" -> ranges={2}")
    @MethodSource("provideMatchCases")
    fun `mentionRegex finds the expected ranges`(
        display: String,
        text: String,
        expectedRanges: List<IntRange>,
    ) {
        val matches = mentionRegex(display).findAll(text).map { it.range }.toList()

        matches `should be equal to` expectedRanges
    }

    companion object {

        @JvmStatic
        @Suppress("LongMethod")
        fun provideMatchCases(): List<Arguments> = listOf(
            // Empty display must never match a bare @.
            Arguments.of("", "hello @ world", emptyList<IntRange>()),
            Arguments.of("", "@", emptyList<IntRange>()),
            // Basic match preceded by space.
            Arguments.of("alice", "hello @alice!", listOf(6..11)),
            // Match at the start of the string.
            Arguments.of("alice", "@alice hi", listOf(0..5)),
            // Match at the end of the string.
            Arguments.of("alice", "hello @alice", listOf(6..11)),
            // Match as the entire string.
            Arguments.of("alice", "@alice", listOf(0..5)),
            // Two consecutive mentions of the same display separated by space.
            Arguments.of("alice", "@alice @alice", listOf(0..5, 7..12)),
            // ASCII suffix prevents a false match (regression for "@Chewbacca" in "@Chewbaccaa").
            Arguments.of("Chewbacca", "W @Chewbacca @Chewbaccaa", listOf(2..11)),
            // ASCII prefix prevents a false match (regression for Compose missing leading boundary).
            Arguments.of("alice", "foo@alice bar", emptyList<IntRange>()),
            // Non-ASCII letter after the display name must NOT be treated as a word boundary.
            Arguments.of("alice", "@aliceé", emptyList<IntRange>()),
            Arguments.of("Chewbacca", "@Chewbaccaé", emptyList<IntRange>()),
            // Non-ASCII letter before the display name must NOT allow a mid-word match.
            Arguments.of("alice", "José@alice", emptyList<IntRange>()),
            // Non-ASCII letter as separator is treated like any other non-word boundary char would be:
            // a letter on either side means "still inside a word", so no match.
            Arguments.of("alice", "Я@alice", emptyList<IntRange>()),
            // Punctuation neighbours are fine on both sides.
            Arguments.of("alice", "(@alice)", listOf(1..6)),
            Arguments.of("alice", "@alice.", listOf(0..5)),
            Arguments.of("alice", "@alice,next", listOf(0..5)),
            // Display name containing a space (e.g. "@John Doe").
            Arguments.of("John Doe", "hi @John Doe!", listOf(3..11)),
            // Display name ending in a non-word character (regression for `\b` at the tail).
            Arguments.of("admins!", "ping @admins! now", listOf(5..12)),
            // Underscore is treated as a word character — adjacent underscore blocks the match.
            Arguments.of("alice", "@alice_smith", emptyList<IntRange>()),
            // Digit suffix is a word character — blocks the match.
            Arguments.of("alice", "@alice1", emptyList<IntRange>()),
            // The match must be case-sensitive.
            Arguments.of("Alice", "@alice", emptyList<IntRange>()),
            // Special regex characters in the display name must be matched literally.
            Arguments.of("a.b", "hi @a.b!", listOf(3..6)),
            Arguments.of("a.b", "hi @aXb!", emptyList<IntRange>()),
            // @channel-style mentions.
            Arguments.of("channel", "hey @channel", listOf(4..11)),
            Arguments.of("here", "@here and @herein", listOf(0..4)),
        )
    }
}
