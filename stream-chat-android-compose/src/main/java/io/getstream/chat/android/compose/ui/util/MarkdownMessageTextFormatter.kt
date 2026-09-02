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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import io.getstream.chat.android.compose.ui.theme.MessageStyling
import io.getstream.chat.android.compose.ui.theme.StreamDesign
import io.getstream.chat.android.compose.ui.util.internal.MarkdownRenderer
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.utils.extensions.isMine

/**
 * A [MessageTextFormatter] that renders the message text as GitHub Flavored Markdown.
 *
 * Install it in place of the default formatter:
 * ```
 * ChatTheme(messageTextFormatter = MarkdownMessageTextFormatter.defaultFormatter(autoTranslationEnabled = true))
 * ```
 *
 * Mentions, links and emails are highlighted on top of the rendered markdown, exactly as they are
 * without it.
 *
 * Rendering changes the length of the text in both directions: syntax characters are dropped, and
 * markers such as a quote's are added. Offsets into the result therefore do not line up with
 * [Message.text], so this formatter replaces the default rather than being combined with it
 * through [MessageTextFormatter.composite], which styles by offsets into the original.
 *
 * ### One deliberate departure from the specification
 *
 * A single line break inside a paragraph renders as a line break, where the specification calls for
 * a soft break that collapses to a space. Complying would mean a line break could only be written
 * as two trailing spaces or a trailing backslash, which is not something anyone can type on a phone
 * keyboard, and it would reflow every multi-line message that renders correctly as plain text
 * today. Chat clients broadly make the same choice, and so does the View-based UI kit, which
 * configures Markwon with `SoftBreakAddsNewLinePlugin`.
 *
 * ### Constructs a single styled string cannot express
 *
 * Images render as their alt text, which is the fallback the specification defines for an image
 * that cannot be shown. Tables keep their source text, and a task list keeps its `[ ]` marker
 * rather than becoming a checkbox. Drawing any of the three needs real layout, which means
 * rendering message text as a tree of composables instead of one styled string.
 */
// Mirrors the parameters of the default message text formatter, plus the markdown styling.
@Suppress("LongParameterList")
public class MarkdownMessageTextFormatter internal constructor(
    private val autoTranslationEnabled: Boolean,
    private val colors: StreamDesign.Colors,
    private val typography: StreamDesign.Typography,
    private val styles: MarkdownStyles,
    private val textStyle: (isMine: Boolean, message: Message) -> TextStyle,
    private val linkStyle: (isMine: Boolean) -> TextStyle,
    private val mentionColor: (isMine: Boolean) -> Color,
    private val builder: AnnotatedMessageTextBuilder?,
) : MessageTextFormatter {

    private val renderer = MarkdownRenderer(styles)

    override fun format(message: Message, currentUser: User?): AnnotatedString {
        val displayedText = message.resolveDisplayedText(currentUser, autoTranslationEnabled)
        val isMine = message.isMine(currentUser)
        val baseStyle = SpanStyle(
            fontStyle = typography.bodyDefault.fontStyle,
            color = textStyle(isMine, message).color,
        )
        val markdown = renderer.render(displayedText)
        // Only the colour and decoration, because a full text style would overwrite the weight,
        // size and family that markdown put underneath the link.
        val linkSpan = linkStyle(isMine).let { SpanStyle(color = it.color, textDecoration = it.textDecoration) }
        return buildAnnotatedString {
            append(markdown.text)
            // The base style goes on first so the markdown spans layered over it win.
            addStyle(baseStyle, start = 0, end = markdown.text.length)
            markdown.spanStyles.forEach { addStyle(it.item, it.start, it.end) }
            markdown.getStringAnnotations(0, markdown.length).forEach {
                addStringAnnotation(it.tag, it.item, it.start, it.end)
                // Links the markdown carried are styled here, since the entity pass below leaves
                // already-annotated ranges alone.
                if (it.tag == AnnotationTagUrl) addStyle(linkSpan, it.start, it.end)
            }
        }
            .annotateStreamEntities(
                message = message,
                colors = colors,
                linkStyle = linkStyle(isMine),
                mentionColor = mentionColor(isMine),
            )
            .let { annotated ->
                val extra = builder ?: return@let annotated
                buildAnnotatedString {
                    append(annotated)
                    extra.invoke(this, message, currentUser)
                }
            }
    }

    public companion object {

        /**
         * Builds a markdown formatter styled from the design system, mirroring the parameters of
         * [MessageTextFormatter.defaultFormatter].
         *
         * @param autoTranslationEnabled Whether the auto-translation is enabled.
         * @param isInDarkMode Whether the app is in dark mode.
         * @param typography The typography to use for styling.
         * @param colors The colors to use for styling.
         * @param styles The styling applied to the markdown constructs.
         * @param textStyle The text style to use for styling.
         * @param mentionColor Return [Color.Unspecified] (the default) to use per-type tokens from
         * [colors]; return a specified color to apply it to every mention.
         * @param builder The builder to use for customizing the text.
         */
        @Composable
        public fun defaultFormatter(
            autoTranslationEnabled: Boolean,
            isInDarkMode: Boolean = isSystemInDarkTheme(),
            typography: StreamDesign.Typography = StreamDesign.Typography.default(),
            colors: StreamDesign.Colors = when (isInDarkMode) {
                true -> StreamDesign.Colors.defaultDark()
                else -> StreamDesign.Colors.default()
            },
            styles: MarkdownStyles = MarkdownStyles.defaults(typography = typography, colors = colors),
            textStyle: (isMine: Boolean, message: Message) -> TextStyle =
                { isMine, _ -> MessageStyling.textStyle(outgoing = isMine, typography, colors) },
            linkStyle: (isMine: Boolean) -> TextStyle = { MessageStyling.linkStyle(typography, colors) },
            mentionColor: (isMine: Boolean) -> Color = { Color.Unspecified },
            builder: AnnotatedMessageTextBuilder? = null,
        ): MarkdownMessageTextFormatter = MarkdownMessageTextFormatter(
            autoTranslationEnabled = autoTranslationEnabled,
            colors = colors,
            typography = typography,
            styles = styles,
            textStyle = textStyle,
            linkStyle = linkStyle,
            mentionColor = mentionColor,
            builder = builder,
        )
    }
}
