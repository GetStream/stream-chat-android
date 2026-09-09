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
 * The tag marking text to take literally, such as markdown code, so that nothing inside it is
 * detected as a URL, an email or a mention.
 */
internal const val AnnotationTagLiteral: AnnotationTag = "LITERAL"

/** Marks a block quote's span, carrying its depth of nesting, so its rail can be drawn. */
internal const val AnnotationTagBlockQuote: AnnotationTag = "BLOCK_QUOTE"

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

        annotateEntities(
            text = text,
            linkStyle = linkStyle,
            mentionsColor = mentionsColor,
            mentionedUserNames = mentionedUserNames,
        )

        // Finally, we apply any additional styling that was passed in.
        builder(this)
    }
}

/**
 * Adds the annotations Stream recognises in message text, URLs, emails and the mentions named by
 * [mentionedUserNames], on top of an already styled string.
 *
 * Ranges already tagged [AnnotationTagUrl] keep the destination they were built with, and ranges
 * tagged [AnnotationTagLiteral] are left as written.
 *
 * @param mentionedUserNames The names to highlight, each without its leading `@`.
 * @param linkStyle The style applied to URLs and emails.
 * @param mentionsColor Applied to every mention.
 */
internal fun AnnotatedString.annotateStreamEntities(
    mentionedUserNames: List<String>,
    linkStyle: TextStyle,
    mentionsColor: Color,
): AnnotatedString {
    val styled = this
    return buildAnnotatedString {
        append(styled.text)
        addSpanStyles(styled.spanStyles)
        addParagraphStyles(styled.paragraphStyles)
        // Everything but the literal markers, which exist only for the pass below.
        addStringAnnotations(styled.stringAnnotations.filter { it.tag != AnnotationTagLiteral })
        annotateEntities(
            text = styled.text,
            linkStyle = linkStyle,
            mentionsColor = mentionsColor,
            mentionedUserNames = mentionedUserNames,
            skipRanges = styled.stringAnnotations
                .filter { it.tag == AnnotationTagUrl || it.tag == AnnotationTagLiteral }
                .map { it.start until it.end },
        )
    }
}

/**
 * Styles and annotates every URL, email and mention in [text], which the receiver must already
 * hold as its content for the match offsets to line up.
 */
@SuppressLint("RestrictedApi")
private fun AnnotatedString.Builder.annotateEntities(
    text: String,
    linkStyle: TextStyle,
    mentionsColor: Color,
    mentionedUserNames: List<String>,
    skipRanges: List<IntRange> = emptyList(),
) {
    // For each available link in the text, we add a different style, to represent the links,
    // as well as add a String annotation to it. This gives us the ability to open the URL on click.
    linkify(
        text = text,
        tag = AnnotationTagUrl,
        pattern = PatternsCompat.AUTOLINK_WEB_URL,
        matchFilter = Linkify.sUrlMatchFilter,
        schemes = URL_SCHEMES,
        textStyle = linkStyle,
        skipRanges = skipRanges,
    )
    linkify(
        text = text,
        tag = AnnotationTagEmail,
        pattern = PatternsCompat.AUTOLINK_EMAIL_ADDRESS,
        schemes = EMAIL_SCHEMES,
        textStyle = linkStyle,
        skipRanges = skipRanges,
    )
    tagUser(
        text = text,
        mentionsColor = mentionsColor,
        mentionedUserNames = mentionedUserNames,
        skipRanges = skipRanges,
    )
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
    skipRanges: List<IntRange> = emptyList(),
) {
    @SuppressLint("RestrictedApi")
    val matcher = pattern.matcher(text)
    while (matcher.find()) {
        val start = matcher.start()
        val end = matcher.end()

        val rejected = (matchFilter != null && !matchFilter.acceptMatch(text, start, end)) ||
            skipRanges.any { start <= it.last && it.first < end }
        if (rejected) {
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
    skipRanges: List<IntRange> = emptyList(),
) {
    mentionedUserNames.forEach { userName ->
        if (userName.isEmpty()) return@forEach
        // Every occurrence, so skipping one inside a code span still leaves the others tappable.
        mentionTokenRegex(userName).findAll(text).forEach { match ->
            val start = match.range.first
            val end = match.range.last + 1
            if (skipRanges.any { start <= it.last && it.first < end }) return@forEach

            addStyle(
                style = SpanStyle(
                    color = mentionsColor,
                    fontWeight = FontWeight.Bold,
                ),
                start = start,
                end = end,
            )

            addStringAnnotation(
                tag = AnnotationTagMention,
                annotation = userName,
                start = start,
                end = end,
            )
        }
    }
}

/**
 * Matches the `@<display>` token, the `@` included, so the range never has to be walked back past
 * the start of the text. The boundaries use Unicode-aware lookaround, because Java's word-boundary
 * classes only recognise ASCII.
 */
private fun mentionTokenRegex(display: String): Regex =
    Regex("(?<![\\p{L}\\p{N}_])@${Regex.escape(display)}(?![\\p{L}\\p{N}_])")

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
