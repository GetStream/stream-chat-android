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

import android.annotation.SuppressLint
import android.text.util.Linkify
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.core.util.PatternsCompat
import io.getstream.chat.android.compose.ui.theme.MentionStyleFactory
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.Mention
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
 * Builds an [AnnotatedString] from a given text, applying styles and annotations for links and mentions.
 * Used in message bubbles.
 *
 * @param text The input text to be transformed into an [AnnotatedString].
 * @param textColor The color to be applied to the regular text.
 * @param textFontStyle The font style to be applied to the regular text.
 * @param linkStyle The text style to be applied to links within the text.
 * @param mentionsColor The color to be applied to mentions within the text.
 * @param mentionedUserNames A list of usernames that are mentioned in the text.
 * @param builder An optional lambda to apply additional styles or annotations.
 */
@SuppressLint("RestrictedApi")
internal fun buildAnnotatedMessageText(
    text: String,
    textColor: Color,
    textFontStyle: FontStyle?,
    linkStyle: TextStyle,
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
            textStyle = linkStyle,
        )
        linkify(
            text = text,
            tag = AnnotationTagEmail,
            pattern = PatternsCompat.AUTOLINK_EMAIL_ADDRESS,
            schemes = EMAIL_SCHEMES,
            textStyle = linkStyle,
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
 * Builds an [AnnotatedString] from a given text, applying styles and annotations for links and mentions.
 * Used in message input fields.
 *
 * @param text The input text to be transformed into an [AnnotatedString].
 * @param textColor The color to be applied to the regular text.
 * @param textFontStyle The font style to be applied to the regular text.
 * @param linkStyle The text style to be applied to links within the text.
 * @param mentions A set of [Mention] objects representing the mentions in the text.
 * @param mentionStyleFactory A factory to provide styles for mentions.
 * @param builder An optional lambda to apply additional styles or annotations.
 */
@SuppressLint("RestrictedApi")
internal fun buildAnnotatedInputText(
    text: String,
    textColor: Color,
    textFontStyle: FontStyle?,
    linkStyle: TextStyle,
    mentions: Set<Mention> = emptySet(),
    mentionStyleFactory: MentionStyleFactory = MentionStyleFactory.NoStyle,
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
            textStyle = linkStyle,
        )
        linkify(
            text = text,
            tag = AnnotationTagEmail,
            pattern = PatternsCompat.AUTOLINK_EMAIL_ADDRESS,
            schemes = EMAIL_SCHEMES,
            textStyle = linkStyle,
        )
        tagMentions(
            text = text,
            mentions = mentions,
            mentionStyleFactory = mentionStyleFactory,
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
    textStyle: TextStyle,
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
            style = textStyle.toSpanStyle(),
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

private fun AnnotatedString.Builder.tagMentions(
    text: String,
    mentions: Set<Mention>,
    mentionStyleFactory: MentionStyleFactory,
) {
    mentions.forEach { mention ->
        val start = text.indexOf(mention.display)
        val end = start + mention.display.length
        if (start < 0) return@forEach

        val style = mentionStyleFactory.styleFor(mention)
        if (style != null) {
            addStyle(style, start - 1, end) // -1 to include the @ symbol
            addStringAnnotation(AnnotationTagMention, mention.display, start - 1, end) // -1 to include the @ symbol
        }
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
