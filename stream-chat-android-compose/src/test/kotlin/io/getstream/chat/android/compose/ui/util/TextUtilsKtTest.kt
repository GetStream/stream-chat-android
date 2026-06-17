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

package io.getstream.chat.android.compose.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import io.getstream.chat.android.compose.ui.theme.StreamDesign
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.randomUserGroup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class TextUtilsKtTest {

    @ParameterizedTest
    @MethodSource("urlArguments")
    fun `Verify that only the scheme should be lowercase`(
        url: String,
        schemes: List<String>,
        expectedResult: String,
    ) {
        assertEquals(expectedResult, url.ensureLowercaseScheme(schemes))
    }

    @Test
    fun `buildAnnotatedMessageText should annotate URLs, emails, and mentions correctly`() {
        // Given
        val text = "Check out https://getstream.io and contact support@getstream.io or ask @John for help"
        val textColor = Color.Black
        val textFontStyle = FontStyle.Normal
        val linkStyle = TextStyle(color = Color.Blue)
        val mentionsColor = Color.Red
        val mentions = listOf(
            TextMention(
                token = "John",
                annotationTag = AnnotationTagUserMention,
                color = mentionsColor,
                background = Color.Unspecified,
            ),
        )

        // When
        val result = buildAnnotatedMessageText(
            text = text,
            textColor = textColor,
            textFontStyle = textFontStyle,
            linkStyle = linkStyle,
            mentions = mentions,
        )

        // Then
        assertEquals(text, result.text)

        // Verify URL annotation
        val urlAnnotations = result.getStringAnnotations(AnnotationTagUrl, 0, text.length)
        assertEquals(1, urlAnnotations.size)
        assertEquals("https://getstream.io", urlAnnotations[0].item)
        assertEquals(10, urlAnnotations[0].start) // Position of "https://getstream.io"
        assertEquals(30, urlAnnotations[0].end)

        // Verify email annotation
        val emailAnnotations = result.getStringAnnotations(AnnotationTagEmail, 0, text.length)
        assertEquals(1, emailAnnotations.size)
        assertEquals("mailto:support@getstream.io", emailAnnotations[0].item)
        assertEquals(43, emailAnnotations[0].start) // Position of "support@getstream.io"
        assertEquals(63, emailAnnotations[0].end)

        // Verify mention annotation
        val mentionAnnotations = result.getStringAnnotations(AnnotationTagUserMention, 0, text.length)
        assertEquals(1, mentionAnnotations.size)
        assertEquals("John", mentionAnnotations[0].item)
        assertEquals(71, mentionAnnotations[0].start) // Position of "@John" (includes @)
        assertEquals(76, mentionAnnotations[0].end)
    }

    @Test
    fun `collectTextMentions emits no entries for a message without mentions`() {
        val message = randomMessage(
            mentionedUsers = emptyList(),
            mentionedChannel = false,
            mentionedHere = false,
            mentionedRoles = emptyList(),
            mentionedGroups = emptyList(),
        )

        val result = message.collectTextMentions(colors = StreamDesign.Colors.default())

        assertEquals(emptyList<TextMention>(), result)
    }

    @Test
    fun `collectTextMentions emits a user mention token from name`() {
        val message = randomMessage(
            mentionedUsers = listOf(randomUser(id = "u1", name = "John")),
            mentionedChannel = false,
            mentionedHere = false,
        )

        val result = message.collectTextMentions(colors = StreamDesign.Colors.default())

        assertEquals(1, result.size)
        assertEquals("John", result[0].token)
        assertEquals(AnnotationTagUserMention, result[0].annotationTag)
    }

    @Test
    fun `collectTextMentions falls back to user id when name is empty`() {
        val message = randomMessage(
            mentionedUsers = listOf(randomUser(id = "u1", name = "")),
            mentionedChannel = false,
            mentionedHere = false,
        )

        val result = message.collectTextMentions(colors = StreamDesign.Colors.default())

        assertEquals(1, result.size)
        assertEquals("u1", result[0].token)
        assertEquals(AnnotationTagUserMention, result[0].annotationTag)
    }

    @ParameterizedTest
    @MethodSource("nonUserMentionCases")
    fun `collectTextMentions maps each non-user mention kind to its token and tag`(
        message: Message,
        expectedToken: String,
        expectedTag: AnnotationTag,
    ) {
        val result = message.collectTextMentions(colors = StreamDesign.Colors.default())

        assertEquals(1, result.size)
        assertEquals(expectedToken, result[0].token)
        assertEquals(expectedTag, result[0].annotationTag)
    }

    @Test
    fun `collectTextMentions emits name and id tokens for a group when they differ`() {
        val message = randomMessage(
            mentionedUsers = emptyList(),
            mentionedChannel = false,
            mentionedHere = false,
            mentionedRoles = emptyList(),
            mentionedGroups = listOf(randomUserGroup(id = "backendsupport", name = "Backend Support Team")),
        )

        val result = message.collectTextMentions(colors = StreamDesign.Colors.default())

        assertEquals(2, result.size)
        assertEquals(listOf("Backend Support Team", "backendsupport"), result.map { it.token })
        assertEquals(List(2) { AnnotationTagGroupMention }, result.map { it.annotationTag })
    }

    @Test
    fun `collectTextMentions applies textColorOverride when specified`() {
        val message = randomMessage(mentionedChannel = true, mentionedHere = false, mentionedUsers = emptyList())
        val override = Color.Red

        val result = message.collectTextMentions(
            colors = StreamDesign.Colors.default(),
            textColorOverride = override,
        )

        assertEquals(override, result[0].color)
    }

    @Test
    fun `buildAnnotatedInputText should annotate URLs and emails correctly`() {
        // Given
        val text = "Visit https://example.com or email test@example.com"
        val textColor = Color.Black
        val textFontStyle = FontStyle.Normal
        val linkStyle = TextStyle(color = Color.Blue, fontWeight = FontWeight.Bold)

        // When
        val result = buildAnnotatedInputText(
            text = text,
            textColor = textColor,
            textFontStyle = textFontStyle,
            linkStyle = linkStyle,
        )

        // Then
        assertEquals(text, result.text)

        // Verify URL annotation
        val urlAnnotations = result.getStringAnnotations(AnnotationTagUrl, 0, text.length)
        assertEquals(1, urlAnnotations.size)
        assertEquals("https://example.com", urlAnnotations[0].item)
        assertEquals(6, urlAnnotations[0].start)
        assertEquals(25, urlAnnotations[0].end)

        // Verify email annotation
        val emailAnnotations = result.getStringAnnotations(AnnotationTagEmail, 0, text.length)
        assertEquals(1, emailAnnotations.size)
        assertEquals("mailto:test@example.com", emailAnnotations[0].item)
        assertEquals(35, emailAnnotations[0].start)
        assertEquals(51, emailAnnotations[0].end)
    }

    companion object {

        @JvmStatic
        fun nonUserMentionCases(): List<Arguments> {
            val empty = randomMessage(
                mentionedUsers = emptyList(),
                mentionedChannel = false,
                mentionedHere = false,
                mentionedRoles = emptyList(),
                mentionedGroups = emptyList(),
            )
            return listOf(
                Arguments.of(empty.copy(mentionedChannel = true), "channel", AnnotationTagChannelMention),
                Arguments.of(empty.copy(mentionedHere = true), "here", AnnotationTagHereMention),
                Arguments.of(empty.copy(mentionedRoles = listOf("admin")), "admin", AnnotationTagRoleMention),
                Arguments.of(
                    empty.copy(mentionedGroups = listOf(randomUserGroup(id = "backend", name = "backend"))),
                    "backend",
                    AnnotationTagGroupMention,
                ),
            )
        }

        @JvmStatic
        fun urlArguments() = listOf(
            Arguments.of(
                "http://www.getstream.io",
                listOf("https://", "http://"),
                "http://www.getstream.io",
            ),
            Arguments.of(
                "https://www.getstream.io",
                listOf("https://", "http://"),
                "https://www.getstream.io",
            ),
            Arguments.of(
                "HTTPS://www.getstream.io",
                listOf("https://", "http://"),
                "https://www.getstream.io",
            ),
            Arguments.of(
                "HTtPS://www.getstream.io",
                listOf("https://", "http://"),
                "https://www.getstream.io",
            ),
            Arguments.of(
                "HTtPS://www.getstream.io/SomePath",
                listOf("https://", "http://"),
                "https://www.getstream.io/SomePath",
            ),
            Arguments.of(
                "www.getstream.io/SomePath",
                listOf("https://", "http://"),
                "https://www.getstream.io/SomePath",
            ),
        )
    }
}
