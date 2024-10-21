/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

import android.annotation.SuppressLint
import android.text.util.Linkify
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.core.util.PatternsCompat
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import java.util.regex.Pattern

internal typealias AnnotationTag = String

/**
 * The tag used to annotate URLs in the message text.
 */
internal const val AnnotationTagUrl: AnnotationTag = "URL"

/**
 * The tag used to annotate emails in the message text.
 */
internal const val AnnotationTagEmail: AnnotationTag = "EMAIL"

/**
 * The tag used to annotate mentions in the message text.
 */
internal const val AnnotationTagMention: AnnotationTag = "MENTION"

/**
 * Takes the given message text and builds an annotated message text that shows links and allows for clicks,
 * if there are any links available.
 *
 * @param text The message text to style.
 * @param color The color of the message text.
 *
 * @return The annotated String, with clickable links, if applicable.
 */
@Composable
@SuppressLint("RestrictedApi")
internal fun buildAnnotatedMessageText(
    text: String,
    color: Color,
): AnnotatedString {
    return buildAnnotatedMessageText(
        text = text,
        textColor = color,
        textFontStyle = ChatTheme.typography.body.fontStyle,
        linkColor = ChatTheme.colors.primaryAccent,
        mentionsColor = ChatTheme.colors.primaryAccent,
    )
}

@SuppressLint("RestrictedApi")
internal fun buildAnnotatedMessageText(
    text: String,
    textColor: Color,
    textFontStyle: FontStyle?,
    linkColor: Color,
    mentionsColor: Color,
    mentionedUserNames: List<String> = emptyList(),
    builder: (AnnotatedString.Builder).() -> Unit = {},
): AnnotatedString {
    return buildAnnotatedString {
        // First we add the whole text to the [AnnotatedString] and style it as a regular text.
        append(text)
        addStyle(
            SpanStyle(
                fontStyle = textFontStyle,
                color = textColor,
            ),
            start = 0,
            end = text.length,
        )

        // Then for each available link in the text, we add a different style, to represent the links,
        // as well as add a String annotation to it. This gives us the ability to open the URL on click.
        linkify(
            text = text,
            tag = AnnotationTagUrl,
            pattern = PatternsCompat.AUTOLINK_WEB_URL,
            matchFilter = Linkify.sUrlMatchFilter,
            schemes = URL_SCHEMES,
            linkColor = linkColor,
        )
        linkify(
            text = text,
            tag = AnnotationTagEmail,
            pattern = PatternsCompat.AUTOLINK_EMAIL_ADDRESS,
            schemes = EMAIL_SCHEMES,
            linkColor = linkColor,
        )
        tagUser(
            text = text,
            mentionsColor = mentionsColor,
            mentionedUserNames = mentionedUserNames,
        )

        // Finally, we apply any additional styling that was passed in.
        builder(this)
    }
}

/**
 * Transforms a given [String] containing bold (<b>...</b>) tags to an [AnnotatedString] to be rendered in Compose
 * components.
 */
internal fun String.parseBoldTags(): AnnotatedString {
    val parts = this.split("<b>", "</b>")
    return buildAnnotatedString {
        var inBoldPart = false
        for (part in parts) {
            if (inBoldPart) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(part)
                }
            } else {
                append(part)
            }
            inBoldPart = !inBoldPart
        }
    }
}

private fun AnnotatedString.Builder.linkify(
    text: CharSequence,
    tag: String,
    pattern: Pattern,
    matchFilter: Linkify.MatchFilter? = null,
    schemes: List<String>,
    linkColor: Color,
) {
    @SuppressLint("RestrictedApi")
    val matcher = pattern.matcher(text)
    while (matcher.find()) {
        val start = matcher.start()
        val end = matcher.end()

        if (matchFilter != null && !matchFilter.acceptMatch(text, start, end)) {
            continue
        }

        addStyle(
            style = SpanStyle(
                color = linkColor,
                textDecoration = TextDecoration.Underline,
            ),
            start = start,
            end = end,
        )

        val linkText = requireNotNull(matcher.group(0)!!)

        val url = linkText.ensureLowercaseScheme(schemes)

        addStringAnnotation(
            tag = tag,
            annotation = url,
            start = start,
            end = end,
        )
    }
}

private fun AnnotatedString.Builder.tagUser(
    text: String,
    mentionsColor: Color,
    mentionedUserNames: List<String>,
) {
    mentionedUserNames.forEach { userName ->
        val start = text.indexOf(userName)
        val end = start + userName.length

        if (start < 0) return@forEach

        addStyle(
            style = SpanStyle(
                color = mentionsColor,
                fontWeight = FontWeight.Bold,
            ),
            start = start - 1, // -1 to include the @ symbol
            end = end,
        )

        addStringAnnotation(
            tag = AnnotationTagMention,
            annotation = userName,
            start = start - 1, // -1 to include the @ symbol
            end = end,
        )
    }
}

internal fun String.ensureLowercaseScheme(schemes: List<String>): String =
    schemes.fold(this) { acc, scheme ->
        acc.replace(scheme, scheme.lowercase(), ignoreCase = true)
    }.let { url ->
        if (schemes.none { url.startsWith(it) }) {
            schemes[0].lowercase() + url
        } else {
            url
        }
    }

private val URL_SCHEMES = listOf("http://", "https://")
private val EMAIL_SCHEMES = listOf("mailto:")
