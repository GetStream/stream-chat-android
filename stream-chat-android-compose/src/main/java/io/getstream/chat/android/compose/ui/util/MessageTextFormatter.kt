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
import androidx.compose.ui.text.TextStyle
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageStyling
import io.getstream.chat.android.compose.ui.theme.StreamDesign
import io.getstream.chat.android.compose.ui.theme.TranslationConfig
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.utils.extensions.isMine

/**
 * An interface that allows to format the message text.
 */
public fun interface MessageTextFormatter {

    /**
     * Formats the given message text.
     *
     * @param message The message to format.
     * @param currentUser The currently logged in user.
     * @return The formatted message text.
     */
    public fun format(message: Message, currentUser: User?): AnnotatedString

    public companion object {

        /**
         * Builds the default message text formatter.
         *
         * @param autoTranslationEnabled Whether the auto-translation is enabled.
         * @param isInDarkMode Whether the app is in dark mode.
         * @param typography The typography to use for styling.
         * @param colors The colors to use for styling.
         * @param textStyle The text style to use for styling.
         * @param mentionColor Return [Color.Unspecified] (the default) to use per-type tokens from
         * [colors]; return a specified color to apply it to every mention.
         * @param builder The builder to use for customizing the text.
         * @return The default implementation of [MessageTextFormatter].
         *
         * @see [DefaultMessageTextFormatter]
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
            textStyle: (isMine: Boolean, message: Message) -> TextStyle =
                { isMine, _ -> MessageStyling.textStyle(outgoing = isMine, typography, colors) },
            linkStyle: (isMine: Boolean) -> TextStyle = { MessageStyling.linkStyle(typography, colors) },
            mentionColor: (isMine: Boolean) -> Color = { Color.Unspecified },
            builder: AnnotatedMessageTextBuilder? = null,
        ): MessageTextFormatter {
            return DefaultMessageTextFormatter(
                autoTranslationEnabled = autoTranslationEnabled,
                colors = colors,
                typography = typography,
                textStyle = textStyle,
                linkStyle = linkStyle,
                mentionColor = mentionColor,
                builder = builder,
            )
        }

        /**
         * Builds a formatter that renders the message text as markdown, in place of
         * [defaultFormatter]:
         * ```
         * ChatTheme(
         *     messageTextFormatter = MessageTextFormatter.markdownFormatter(autoTranslationEnabled = true),
         * )
         * ```
         *
         * Mentions, links and emails are highlighted on top of the rendered markdown. Rendering
         * changes the text's length, so offsets no longer line up with [Message.text] and this
         * cannot be combined through [composite], which styles by those offsets. Styling follows
         * [typography] and [colors].
         *
         * A single line break renders as a line break, where the specification collapses it to a
         * space. Complying would leave two trailing spaces as the only way to write one, and would
         * reflow every multi-line message that reads correctly as plain text today. The View-based
         * kit deviates the same way.
         *
         * Emphasis, strikethrough, code, headings, lists, quotes and links are rendered. An image
         * falls back to its alt text, and a table or task list to its source, since none of the
         * three can be drawn in a styled string.
         */
        @Composable
        public fun markdownFormatter(
            autoTranslationEnabled: Boolean,
            isInDarkMode: Boolean = isSystemInDarkTheme(),
            typography: StreamDesign.Typography = StreamDesign.Typography.default(),
            colors: StreamDesign.Colors = when (isInDarkMode) {
                true -> StreamDesign.Colors.defaultDark()
                else -> StreamDesign.Colors.default()
            },
        ): MessageTextFormatter = MarkdownMessageTextFormatter(
            autoTranslationEnabled = autoTranslationEnabled,
            colors = colors,
            typography = typography,
            styles = MarkdownStyles.defaults(typography = typography, colors = colors),
            textStyle = { isMine, _ -> MessageStyling.textStyle(outgoing = isMine, typography, colors) },
            linkStyle = { MessageStyling.linkStyle(typography, colors) },
            mentionColor = { Color.Unspecified },
            builder = null,
        )

        /**
         * Builds a composite message text formatter.
         *
         * @param formatters The list of formatters to use.
         * @return The composite implementation of [MessageTextFormatter].
         *
         * @see [CompositeMessageTextFormatter]
         */
        public fun composite(vararg formatters: MessageTextFormatter): MessageTextFormatter {
            return CompositeMessageTextFormatter(formatters.toList())
        }
    }
}

/**
 * A builder for the annotated message text.
 */
public typealias AnnotatedMessageTextBuilder = AnnotatedString.Builder.(message: Message, currentUser: User?) -> Unit

private class CompositeMessageTextFormatter(
    private val formatters: List<MessageTextFormatter>,
) : MessageTextFormatter {

    override fun format(message: Message, currentUser: User?): AnnotatedString {
        val builder = AnnotatedString.Builder(message.text)
        for (formatter in formatters) {
            builder.merge(formatter.format(message, currentUser))
        }
        return builder.toAnnotatedString()
    }
}

/**
 * Default implementation of [MessageTextFormatter].
 *
 * The default implementation automatically supports the [TranslationConfig] auto-translation feature.
 * It also uses the [ChatTheme] to style the text including links highlighting.
 */
private class DefaultMessageTextFormatter(
    private val autoTranslationEnabled: Boolean,
    private val colors: StreamDesign.Colors,
    private val typography: StreamDesign.Typography,
    private val textStyle: (isMine: Boolean, message: Message) -> TextStyle,
    private val linkStyle: (isMine: Boolean) -> TextStyle,
    private val mentionColor: (isMine: Boolean) -> Color,
    private val builder: AnnotatedMessageTextBuilder? = null,
) : MessageTextFormatter {

    override fun format(message: Message, currentUser: User?): AnnotatedString {
        val displayedText = message.resolveDisplayedText(currentUser, autoTranslationEnabled)
        val isMine = message.isMine(currentUser)
        val textColor = textStyle(isMine, message).color
        val linkStyle = linkStyle(isMine)
        return buildAnnotatedMessageText(
            text = displayedText,
            textColor = textColor,
            textFontStyle = typography.bodyDefault.fontStyle,
            linkStyle = linkStyle,
            mentions = message.collectTextMentions(colors = colors, textColorOverride = mentionColor(isMine)),
            builder = {
                builder?.invoke(this, message, currentUser)
            },
        )
    }
}
