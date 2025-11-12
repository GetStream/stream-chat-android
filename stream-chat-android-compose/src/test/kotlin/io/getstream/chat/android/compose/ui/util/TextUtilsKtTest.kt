/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.compose.ui.theme.MentionStyleFactory
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.Mention
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
        val mentionedUserNames = listOf("John")

        // When
        val result = buildAnnotatedMessageText(
            text = text,
            textColor = textColor,
            textFontStyle = textFontStyle,
            linkStyle = linkStyle,
            mentionsColor = mentionsColor,
            mentionedUserNames = mentionedUserNames,
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
        val mentionAnnotations = result.getStringAnnotations(AnnotationTagMention, 0, text.length)
        assertEquals(1, mentionAnnotations.size)
        assertEquals("John", mentionAnnotations[0].item)
        assertEquals(71, mentionAnnotations[0].start) // Position of "@John" (includes @)
        assertEquals(76, mentionAnnotations[0].end)
    }

    @Test
    fun `buildAnnotatedInputText should annotate URLs, emails, and mentions correctly`() {
        // Given
        val text = "Visit https://example.com or email test@example.com and mention @Alice"
        val textColor = Color.Black
        val textFontStyle = FontStyle.Normal
        val linkStyle = TextStyle(color = Color.Blue, fontWeight = FontWeight.Bold)
        val user = User(id = "alice-id", name = "Alice")
        val mentions = setOf(Mention.User(user))
        val mentionStyleFactory = object : MentionStyleFactory {
            override fun styleFor(mention: Mention) = androidx.compose.ui.text.SpanStyle(
                color = Color.Magenta,
                fontWeight = FontWeight.Bold,
            )
        }

        // When
        val result = buildAnnotatedInputText(
            text = text,
            textColor = textColor,
            textFontStyle = textFontStyle,
            linkStyle = linkStyle,
            mentions = mentions,
            mentionStyleFactory = mentionStyleFactory,
        )

        // Then
        assertEquals(text, result.text)

        // Verify URL annotation
        val urlAnnotations = result.getStringAnnotations(AnnotationTagUrl, 0, text.length)
        assertEquals(1, urlAnnotations.size)
        assertEquals("https://example.com", urlAnnotations[0].item)
        assertEquals(6, urlAnnotations[0].start) // Position of "https://example.com"
        assertEquals(25, urlAnnotations[0].end)

        // Verify email annotation
        val emailAnnotations = result.getStringAnnotations(AnnotationTagEmail, 0, text.length)
        assertEquals(1, emailAnnotations.size)
        assertEquals("mailto:test@example.com", emailAnnotations[0].item)
        assertEquals(35, emailAnnotations[0].start) // Position of "test@example.com"
        assertEquals(51, emailAnnotations[0].end)

        // Verify mention annotation
        val mentionAnnotations = result.getStringAnnotations(AnnotationTagMention, 0, text.length)
        assertEquals(1, mentionAnnotations.size)
        assertEquals("Alice", mentionAnnotations[0].item)
        assertEquals(64, mentionAnnotations[0].start) // Position of "@Alice" (includes @)
        assertEquals(70, mentionAnnotations[0].end)

        // Verify mention styling was applied
        val styles = result.spanStyles
        val mentionStyle = styles.firstOrNull {
            it.start == 64 && it.end == 70 && it.item.color == Color.Magenta
        }
        assertNotNull(mentionStyle)
    }

    companion object {

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
