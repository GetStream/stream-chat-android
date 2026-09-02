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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.ui.theme.StreamDesign
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class MarkdownMessageTextFormatterTest {

    private val formatter = MarkdownMessageTextFormatter(
        autoTranslationEnabled = false,
        colors = StreamDesign.Colors.default(),
        typography = StreamDesign.Typography.default(),
        styles = MarkdownStyles(
            heading1 = SpanStyle(fontSize = 30.sp),
            heading2 = SpanStyle(fontSize = 26.sp),
            heading3 = SpanStyle(fontSize = 22.sp),
            heading4 = SpanStyle(fontSize = 18.sp),
            heading5 = SpanStyle(fontSize = 16.sp),
            heading6 = SpanStyle(fontSize = 14.sp),
            codeSpan = SpanStyle(fontFamily = FontFamily.Monospace),
            codeBlock = SpanStyle(fontFamily = FontFamily.Monospace),
            blockQuote = SpanStyle(color = Color.Gray),
        ),
        textStyle = { _, _ -> TextStyle(color = Color.Black) },
        linkStyle = { TextStyle(color = Color.Blue) },
        mentionColor = { Color.Unspecified },
        builder = null,
    )

    private val currentUser = User(id = "me")

    @Test
    fun `highlights a mention alongside markdown`() {
        val mentioned = User(id = "u1", name = "Martin")
        val message = message(text = "**hey** @Martin", mentionedUsers = listOf(mentioned))

        val result = formatter.format(message, currentUser)

        result.text shouldBeEqualTo "hey @Martin"
        result.annotation(UserMentionTag, "@Martin") shouldBeEqualTo "Martin"
        result.spanAt("hey")?.fontWeight shouldBeEqualTo FontWeight.Bold
    }

    @Test
    fun `highlights a mention whose name is itself emphasised`() {
        val mentioned = User(id = "u1", name = "Martin")
        val message = message(text = "hey @**Martin**", mentionedUsers = listOf(mentioned))

        val result = formatter.format(message, currentUser)

        result.text shouldBeEqualTo "hey @Martin"
        result.annotation(UserMentionTag, "@Martin") shouldBeEqualTo "Martin"
        result.spanAt("Martin")?.fontWeight shouldBeEqualTo FontWeight.Bold
    }

    @Test
    fun `linkifies a bare url after markdown has been rendered`() {
        val result = formatter.format(message(text = "see *this*: https://getstream.io"), currentUser)

        result.text shouldBeEqualTo "see this: https://getstream.io"
        result.annotation(AnnotationTagUrl, "https://getstream.io") shouldBeEqualTo "https://getstream.io"
    }

    @Test
    fun `keeps the markdown destination when the link label looks like a url`() {
        val message = message(text = "[https://text-link.com](https://real-link.com)")

        val result = formatter.format(message, currentUser)

        result.text shouldBeEqualTo "https://text-link.com"
        result.annotation(AnnotationTagUrl, "https://text-link.com") shouldBeEqualTo "https://real-link.com"
    }

    @Test
    fun `styles a markdown link like a detected one`() {
        val result = formatter.format(message(text = "see [the docs](https://getstream.io)"), currentUser)

        result.spanAt("the docs")?.color shouldBeEqualTo Color.Blue
    }

    @Test
    fun `linkifies an autolink once its brackets are gone`() {
        val result = formatter.format(message(text = "visit <https://getstream.io> now"), currentUser)

        result.text shouldBeEqualTo "visit https://getstream.io now"
        result.annotation(AnnotationTagUrl, "https://getstream.io") shouldBeEqualTo "https://getstream.io"
    }

    @Test
    fun `leaves plain text with line breaks untouched`() {
        val text = "first line\nsecond line"

        formatter.format(message(text = text), currentUser).text shouldBeEqualTo text
    }

    @Test
    fun `applies the base text color to the whole message`() {
        val result = formatter.format(message(text = "# Title\nbody"), currentUser)

        result.spanAt("body")?.color shouldBeEqualTo Color.Black
    }

    @Test
    fun `renders the translation when auto translation is on`() {
        val translating = MarkdownMessageTextFormatter(
            autoTranslationEnabled = true,
            colors = StreamDesign.Colors.default(),
            typography = StreamDesign.Typography.default(),
            styles = MarkdownStyles(
                heading1 = SpanStyle(),
                heading2 = SpanStyle(),
                heading3 = SpanStyle(),
                heading4 = SpanStyle(),
                heading5 = SpanStyle(),
                heading6 = SpanStyle(),
                codeSpan = SpanStyle(),
                codeBlock = SpanStyle(),
                blockQuote = SpanStyle(),
            ),
            textStyle = { _, _ -> TextStyle(color = Color.Black) },
            linkStyle = { TextStyle(color = Color.Blue) },
            mentionColor = { Color.Unspecified },
            builder = null,
        )
        val message = message(text = "**hello**").copy(i18n = mapOf("it_text" to "**ciao**"))

        val result = translating.format(message, User(id = "me", language = "it"))

        result.text shouldBeEqualTo "ciao"
        result.spanAt("ciao")?.fontWeight shouldBeEqualTo FontWeight.Bold
    }

    private fun message(text: String, mentionedUsers: List<User> = emptyList()) = Message(
        id = "message-id",
        cid = "messaging:channel-id",
        text = text,
        user = User(id = "other"),
        mentionedUsers = mentionedUsers,
    )
}

/** Mirrors the tag the Compose kit annotates user mentions with. */
private const val UserMentionTag = "MENTION"

private fun AnnotatedString.annotation(tag: String, substring: String): String? {
    val start = text.indexOf(substring)
    if (start < 0) return null
    return getStringAnnotations(tag, start, start + substring.length).firstOrNull()?.item
}

private fun AnnotatedString.spanAt(substring: String): SpanStyle? {
    val start = text.indexOf(substring)
    if (start < 0) return null
    val covering = spanStyles.filter { it.start <= start && it.end >= start + substring.length }
    if (covering.isEmpty()) return null
    return covering.map { it.item }.reduce { merged, style -> merged.merge(style) }
}
