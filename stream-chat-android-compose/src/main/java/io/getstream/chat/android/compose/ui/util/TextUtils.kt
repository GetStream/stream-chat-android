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
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.core.util.PatternsCompat
import io.getstream.chat.android.compose.ui.theme.StreamDesign
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.Mention
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.mentionRegex
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
 * The tag used to annotate user mentions (`@<user>`) in the message text.
 */
internal const val AnnotationTagUserMention: AnnotationTag = "MENTION"

/**
 * The tag used to annotate `@channel` mentions in the message text.
 */
internal const val AnnotationTagChannelMention: AnnotationTag = "CHANNEL_MENTION"

/**
 * The tag used to annotate `@here` mentions in the message text.
 */
internal const val AnnotationTagHereMention: AnnotationTag = "HERE_MENTION"

/**
 * The tag used to annotate role mentions (e.g. `@admin`) in the message text. The annotation
 * value is the role name.
 */
internal const val AnnotationTagRoleMention: AnnotationTag = "ROLE_MENTION"

/**
 * The tag used to annotate user-group mentions (e.g. `@backendsupport`) in the message text. The
 * annotation value is the group name.
 */
internal const val AnnotationTagGroupMention: AnnotationTag = "GROUP_MENTION"

/**
 * A single mention to highlight in a message's text.
 *
 * @property token The literal that follows the `@` in the text.
 * @property annotationTag The annotation tag attached to every match.
 */
internal data class TextMention(
    val token: String,
    val annotationTag: AnnotationTag,
    val color: Color,
    val background: Color,
)

/**
 * Builds the list of [TextMention]s to highlight for this message, each pre-populated with its
 * per-type color and background from [colors] (overridden by [textColorOverride] when specified).
 */
internal fun Message.collectTextMentions(
    colors: StreamDesign.Colors,
    textColorOverride: Color = Color.Unspecified,
): List<TextMention> = buildList {
    fun add(token: String, tag: AnnotationTag) {
        add(
            TextMention(
                token = token,
                annotationTag = tag,
                color = textColorOverride.takeOrElse { colors.mentionTextColorFor(tag) },
                background = colors.mentionBackgroundFor(tag),
            ),
        )
    }
    mentionedUsers.forEach { user -> add(user.name.ifEmpty(user::id), AnnotationTagUserMention) }
    if (mentionedChannel) add("channel", AnnotationTagChannelMention)
    if (mentionedHere) add("here", AnnotationTagHereMention)
    mentionedRoles.forEach { role -> add(role, AnnotationTagRoleMention) }
    mentionedGroups.forEach { group ->
        Mention.Group(group).tokens.forEach { token -> add(token, AnnotationTagGroupMention) }
    }
}

internal fun StreamDesign.Colors.mentionTextColorFor(tag: AnnotationTag): Color = when (tag) {
    AnnotationTagUserMention -> chatTextMentionUser
    AnnotationTagChannelMention, AnnotationTagHereMention -> chatTextMentionBroadcast
    AnnotationTagRoleMention -> chatTextMentionRole
    AnnotationTagGroupMention -> chatTextMentionGroup
    else -> chatTextMention
}

internal fun StreamDesign.Colors.mentionBackgroundFor(tag: AnnotationTag): Color = when (tag) {
    AnnotationTagUserMention -> chatBgMentionUser
    AnnotationTagChannelMention, AnnotationTagHereMention -> chatBgMentionBroadcast
    AnnotationTagRoleMention -> chatBgMentionRole
    AnnotationTagGroupMention -> chatBgMentionGroup
    else -> Color.Transparent
}

/**
 * Builds an [AnnotatedString] from a given text, applying styles and annotations for links and mentions.
 * Used in message bubbles.
 *
 * @param text The input text to be transformed into an [AnnotatedString].
 * @param textColor The color to be applied to the regular text.
 * @param textFontStyle The font style to be applied to the regular text.
 * @param linkStyle The text style to be applied to links within the text.
 * @param mentions The list of mentions to highlight in the text.
 * @param builder An optional lambda to apply additional styles or annotations.
 */
@SuppressLint("RestrictedApi")
internal fun buildAnnotatedMessageText(
    text: String,
    textColor: Color,
    textFontStyle: FontStyle?,
    linkStyle: TextStyle,
    mentions: List<TextMention> = emptyList(),
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
        mentions.forEach { mention ->
            tagMention(text = text, mention = mention)
        }

        // Finally, we apply any additional styling that was passed in.
        builder(this)
    }
}

/**
 * Builds an [AnnotatedString] from a given text, applying styles and annotations for links.
 * Used in message input fields.
 *
 * @param text The input text to be transformed into an [AnnotatedString].
 * @param textColor The color to be applied to the regular text.
 * @param textFontStyle The font style to be applied to the regular text.
 * @param linkStyle The text style to be applied to links within the text.
 * @param builder An optional lambda to apply additional styles or annotations.
 */
@SuppressLint("RestrictedApi")
internal fun buildAnnotatedInputText(
    text: String,
    textColor: Color,
    textFontStyle: FontStyle?,
    linkStyle: TextStyle,
    builder: (AnnotatedString.Builder).() -> Unit = {},
): AnnotatedString {
    return buildAnnotatedString {
        append(text)
        addStyle(
            SpanStyle(
                fontStyle = textFontStyle,
                color = textColor,
            ),
            start = 0,
            end = text.length,
        )

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

/**
 * Tags every word-bounded `@<token>` occurrence in [text] with [mention]'s annotation tag and
 * styles it with [mention]'s color.
 */
private fun AnnotatedString.Builder.tagMention(
    text: String,
    mention: TextMention,
) {
    if (mention.token.isEmpty()) return
    val pattern = mentionRegex(mention.token)
    pattern.findAll(text).forEach { match ->
        addStyle(
            style = SpanStyle(color = mention.color, background = mention.background),
            start = match.range.first,
            end = match.range.last + 1,
        )
        addStringAnnotation(
            tag = mention.annotationTag,
            annotation = mention.token,
            start = match.range.first,
            end = match.range.last + 1,
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
