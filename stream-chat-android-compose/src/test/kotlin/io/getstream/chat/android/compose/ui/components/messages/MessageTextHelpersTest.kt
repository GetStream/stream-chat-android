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

package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.ui.text.AnnotatedString
import io.getstream.chat.android.compose.ui.util.AnnotationTagEmail
import io.getstream.chat.android.compose.ui.util.AnnotationTagMention
import io.getstream.chat.android.compose.ui.util.AnnotationTagUrl
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

internal class MessageTextHelpersTest {

    @ParameterizedTest
    @MethodSource("interactiveTagCases")
    fun `isInteractiveTag returns true for URL, email and mention tags`(tag: String, expected: Boolean) {
        val range = AnnotatedString.Range(item = "value", start = 0, end = 5, tag = tag)

        range.isInteractiveTag() `should be equal to` expected
    }

    @Test
    fun `hasInteractiveAt returns false for empty annotation list`() {
        val annotations = emptyList<AnnotatedString.Range<String>>()

        annotations.hasInteractiveAt(offset = 0) `should be equal to` false
    }

    @Test
    fun `hasInteractiveAt returns true when offset falls inside a URL annotation`() {
        val annotations = listOf(urlAt(start = 5, end = 15))

        annotations.hasInteractiveAt(offset = 7) `should be equal to` true
    }

    @Test
    fun `hasInteractiveAt returns true at the inclusive start boundary`() {
        val annotations = listOf(urlAt(start = 5, end = 15))

        annotations.hasInteractiveAt(offset = 5) `should be equal to` true
    }

    @Test
    fun `hasInteractiveAt returns false at the exclusive end boundary`() {
        val annotations = listOf(urlAt(start = 5, end = 15))

        annotations.hasInteractiveAt(offset = 15) `should be equal to` false
    }

    @Test
    fun `hasInteractiveAt returns false just before a URL annotation`() {
        val annotations = listOf(urlAt(start = 5, end = 15))

        annotations.hasInteractiveAt(offset = 4) `should be equal to` false
    }

    @Test
    fun `hasInteractiveAt returns true for a mention annotation`() {
        val annotations = listOf(
            AnnotatedString.Range(item = "user", start = 0, end = 5, tag = AnnotationTagMention),
        )

        annotations.hasInteractiveAt(offset = 2) `should be equal to` true
    }

    @Test
    fun `hasInteractiveAt returns true for an email annotation`() {
        val annotations = listOf(
            AnnotatedString.Range(item = "x@y.z", start = 0, end = 5, tag = AnnotationTagEmail),
        )

        annotations.hasInteractiveAt(offset = 2) `should be equal to` true
    }

    @Test
    fun `hasInteractiveAt ignores annotations with non-interactive tags at the same offset`() {
        val annotations = listOf(
            AnnotatedString.Range(item = "style", start = 0, end = 10, tag = "STYLE"),
        )

        annotations.hasInteractiveAt(offset = 5) `should be equal to` false
    }

    @Test
    fun `hasInteractiveAt returns true when at least one of multiple annotations matches`() {
        val annotations = listOf(
            AnnotatedString.Range(item = "style", start = 0, end = 100, tag = "STYLE"),
            urlAt(start = 5, end = 15),
        )

        annotations.hasInteractiveAt(offset = 7) `should be equal to` true
    }

    @Test
    fun `handleAnnotationClick fires onLinkClick for a URL annotation when callback is set`() {
        val onLinkClick = mock<(Message, String) -> Unit>()
        val onUserMentionClick = mock<(User) -> Unit>()
        val fallback = mock<(String) -> Unit>()
        val message = randomMessage(text = "https://example.com")
        val annotations = listOf(urlAt(start = 0, end = 19))

        handleAnnotationClick(
            annotations = annotations,
            position = 5,
            message = message,
            onLinkClick = onLinkClick,
            onUserMentionClick = onUserMentionClick,
            fallback = fallback,
        )

        verify(onLinkClick).invoke(message, "https://example.com")
        verify(fallback, never()).invoke(any())
        verify(onUserMentionClick, never()).invoke(any())
    }

    @Test
    fun `handleAnnotationClick falls back to default handler when onLinkClick is null`() {
        val onUserMentionClick = mock<(User) -> Unit>()
        val fallback = mock<(String) -> Unit>()
        val message = randomMessage(text = "https://example.com")
        val annotations = listOf(urlAt(start = 0, end = 19))

        handleAnnotationClick(
            annotations = annotations,
            position = 5,
            message = message,
            onLinkClick = null,
            onUserMentionClick = onUserMentionClick,
            fallback = fallback,
        )

        verify(fallback).invoke("https://example.com")
        verify(onUserMentionClick, never()).invoke(any())
    }

    @Test
    fun `handleAnnotationClick fires onLinkClick for an email annotation`() {
        val onLinkClick = mock<(Message, String) -> Unit>()
        val message = randomMessage(text = "alice@example.com")
        val annotations = listOf(
            AnnotatedString.Range(
                item = "mailto:alice@example.com",
                start = 0,
                end = 17,
                tag = AnnotationTagEmail,
            ),
        )

        handleAnnotationClick(
            annotations = annotations,
            position = 3,
            message = message,
            onLinkClick = onLinkClick,
            onUserMentionClick = {},
            fallback = {},
        )

        verify(onLinkClick).invoke(message, "mailto:alice@example.com")
    }

    @Test
    fun `handleAnnotationClick fires onUserMentionClick with the resolved user`() {
        val mentioned = randomUser(name = "alice")
        val onUserMentionClick = mock<(User) -> Unit>()
        val onLinkClick = mock<(Message, String) -> Unit>()
        val message = randomMessage(text = "@alice", mentionedUsers = listOf(mentioned))
        val annotations = listOf(
            AnnotatedString.Range(item = "alice", start = 0, end = 6, tag = AnnotationTagMention),
        )

        handleAnnotationClick(
            annotations = annotations,
            position = 2,
            message = message,
            onLinkClick = onLinkClick,
            onUserMentionClick = onUserMentionClick,
            fallback = {},
        )

        verify(onUserMentionClick).invoke(mentioned)
        verify(onLinkClick, never()).invoke(any(), any())
    }

    @Test
    fun `handleAnnotationClick is a no-op when the mentioned user is not in the message`() {
        val onUserMentionClick = mock<(User) -> Unit>()
        val message = randomMessage(text = "@bob", mentionedUsers = emptyList())
        val annotations = listOf(
            AnnotatedString.Range(item = "bob", start = 0, end = 4, tag = AnnotationTagMention),
        )

        handleAnnotationClick(
            annotations = annotations,
            position = 1,
            message = message,
            onLinkClick = null,
            onUserMentionClick = onUserMentionClick,
            fallback = {},
        )

        verify(onUserMentionClick, never()).invoke(any())
    }

    @Test
    fun `handleAnnotationClick is a no-op when no annotation covers the position`() {
        val onLinkClick = mock<(Message, String) -> Unit>()
        val onUserMentionClick = mock<(User) -> Unit>()
        val fallback = mock<(String) -> Unit>()
        val message = randomMessage(text = "https://example.com after")
        val annotations = listOf(urlAt(start = 0, end = 19))

        handleAnnotationClick(
            annotations = annotations,
            position = 22,
            message = message,
            onLinkClick = onLinkClick,
            onUserMentionClick = onUserMentionClick,
            fallback = fallback,
        )

        verify(onLinkClick, never()).invoke(any(), any())
        verify(onUserMentionClick, never()).invoke(any())
        verify(fallback, never()).invoke(any())
    }

    @Test
    fun `handleAnnotationClick ignores non-interactive tags`() {
        val onLinkClick = mock<(Message, String) -> Unit>()
        val fallback = mock<(String) -> Unit>()
        val message = randomMessage()
        val annotations = listOf(
            AnnotatedString.Range(item = "value", start = 0, end = 10, tag = "STYLE"),
        )

        handleAnnotationClick(
            annotations = annotations,
            position = 5,
            message = message,
            onLinkClick = onLinkClick,
            onUserMentionClick = {},
            fallback = fallback,
        )

        verify(onLinkClick, never()).invoke(any(), any())
        verify(fallback, never()).invoke(any())
    }

    @Test
    fun `handleAnnotationClick prefers an interactive annotation when a non-interactive one overlaps the position`() {
        val onLinkClick = mock<(Message, String) -> Unit>()
        val message = randomMessage(text = "https://example.com")
        // Non-interactive annotation listed first, interactive URL listed second, both cover position 5.
        val annotations = listOf(
            AnnotatedString.Range(item = "decoration", start = 0, end = 19, tag = "STYLE"),
            urlAt(start = 0, end = 19),
        )

        handleAnnotationClick(
            annotations = annotations,
            position = 5,
            message = message,
            onLinkClick = onLinkClick,
            onUserMentionClick = {},
            fallback = {},
        )

        verify(onLinkClick).invoke(message, "https://example.com")
    }

    @Test
    fun `handleAnnotationClick ignores URL annotations with empty item`() {
        val onLinkClick = mock<(Message, String) -> Unit>()
        val fallback = mock<(String) -> Unit>()
        val message = randomMessage()
        val annotations = listOf(
            AnnotatedString.Range(item = "", start = 0, end = 5, tag = AnnotationTagUrl),
        )

        handleAnnotationClick(
            annotations = annotations,
            position = 2,
            message = message,
            onLinkClick = onLinkClick,
            onUserMentionClick = {},
            fallback = fallback,
        )

        verify(onLinkClick, never()).invoke(any(), any())
        verify(fallback, never()).invoke(any())
    }

    private fun urlAt(start: Int, end: Int) =
        AnnotatedString.Range(item = "https://example.com", start = start, end = end, tag = AnnotationTagUrl)

    companion object {

        @JvmStatic
        fun interactiveTagCases(): List<Arguments> = listOf(
            Arguments.of(AnnotationTagUrl, true),
            Arguments.of(AnnotationTagEmail, true),
            Arguments.of(AnnotationTagMention, true),
            Arguments.of("STYLE", false),
            Arguments.of("UNKNOWN", false),
            Arguments.of("", false),
        )
    }
}
