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
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.util.PatternsCompat
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import java.util.Locale
import java.util.regex.Pattern

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
internal fun buildAnnotatedMessageText(text: String, color: Color): AnnotatedString {
    return buildAnnotatedString {
        // First we add the whole text to the [AnnotatedString] and style it as a regular text.
        append(text)
        addStyle(
            SpanStyle(
                fontStyle = ChatTheme.typography.body.fontStyle,
                color = color,
            ),
            start = 0,
            end = text.length,
        )

        // Then for each available link in the text, we add a different style, to represent the links,
        // as well as add a String annotation to it. This gives us the ability to open the URL on click.
        linkify(
            text = text,
            tag = "URL",
            pattern = PatternsCompat.AUTOLINK_WEB_URL,
            matchFilter = Linkify.sUrlMatchFilter,
            schemes = URL_SCHEMES,
            linkColor = ChatTheme.colors.primaryAccent,
        )
        linkify(
            text = text,
            tag = "EMAIL",
            pattern = PatternsCompat.AUTOLINK_EMAIL_ADDRESS,
            schemes = EMAIL_SCHEMES,
            linkColor = ChatTheme.colors.primaryAccent,
        )
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

        val url = linkText.fixPrefix(schemes)

        addStringAnnotation(
            tag = tag,
            annotation = url,
            start = start,
            end = end,
        )
    }
}

private fun String.fixPrefix(schemes: List<String>): String =
    lowercase(Locale.getDefault())
        .let {
            if (schemes.none { scheme -> it.startsWith(scheme) }) {
                schemes[0] + it
            } else {
                it
            }
        }

private val URL_SCHEMES = listOf("http://", "https://")
private val EMAIL_SCHEMES = listOf("mailto:")
