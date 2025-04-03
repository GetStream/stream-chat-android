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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageTheme
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamShapes
import io.getstream.chat.android.compose.ui.theme.StreamTypography
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
         * @param builder The builder to use for customizing the text.
         * @return The default implementation of [MessageTextFormatter].
         *
         * @see [DefaultMessageTextFormatter]
         */
        @Composable
        public fun defaultFormatter(
            autoTranslationEnabled: Boolean,
            isInDarkMode: Boolean = isSystemInDarkTheme(),
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
            textStyle: (isMine: Boolean, message: Message) -> TextStyle =
                defaultTextStyle(isInDarkMode, typography, colors),
            mentionColor: (isMine: Boolean) -> Color = defaultMentionColor(isInDarkMode, typography, colors),
            builder: AnnotatedMessageTextBuilder? = null,
        ): MessageTextFormatter {
            return DefaultMessageTextFormatter(
                autoTranslationEnabled = autoTranslationEnabled,
                typography = typography,
                colors = colors,
                textStyle = textStyle,
                mentionColor = mentionColor,
                builder = builder,
            )
        }

        /**
         * Builds the default message text formatter.
         *
         * @param autoTranslationEnabled Whether the auto-translation is enabled.
         * @param typography The typography to use for styling.
         * @param colors The colors to use for styling.
         * @param ownMessageTheme The theme to use for the current user's messages.
         * @param otherMessageTheme The theme to use for other users' messages.
         * @param builder The builder to use for customizing the text.
         * @return The default implementation of [MessageTextFormatter].
         *
         * @see [DefaultMessageTextFormatter]
         */
        @Composable
        public fun defaultFormatter(
            autoTranslationEnabled: Boolean,
            isInDarkMode: Boolean = isSystemInDarkTheme(),
            typography: StreamTypography = StreamTypography.defaultTypography(),
            shapes: StreamShapes = StreamShapes.defaultShapes(),
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
            ownMessageTheme: MessageTheme = MessageTheme.defaultOwnTheme(
                isInDarkMode = isInDarkMode,
                typography = typography,
                shapes = shapes,
                colors = colors,
            ),
            otherMessageTheme: MessageTheme = MessageTheme.defaultOtherTheme(
                isInDarkMode = isInDarkMode,
                typography = typography,
                shapes = shapes,
                colors = colors,
            ),
            builder: AnnotatedMessageTextBuilder? = null,
        ): MessageTextFormatter {
            val textStyle = defaultTextStyle(ownMessageTheme, otherMessageTheme)
            val mentionColor = defaultMentionColor(ownMessageTheme, otherMessageTheme)
            return defaultFormatter(
                autoTranslationEnabled = autoTranslationEnabled,
                isInDarkMode = isInDarkMode,
                typography = typography,
                colors = colors,
                textStyle = textStyle,
                mentionColor = mentionColor,
                builder = builder,
            )
        }

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
 * The default implementation automatically supports the [ChatTheme.autoTranslationEnabled] feature.
 * It also uses the [ChatTheme] to style the text including links highlighting.
 */
private class DefaultMessageTextFormatter(
    private val autoTranslationEnabled: Boolean,
    private val typography: StreamTypography,
    private val colors: StreamColors,
    private val textStyle: (isMine: Boolean, message: Message) -> TextStyle,
    private val mentionColor: (isMine: Boolean) -> Color,
    private val builder: AnnotatedMessageTextBuilder? = null,
) : MessageTextFormatter {

    override fun format(message: Message, currentUser: User?): AnnotatedString {
        val displayedText = when (autoTranslationEnabled) {
            true -> currentUser?.language?.let { userLanguage ->
                message.getTranslation(userLanguage).ifEmpty { message.text }
            } ?: message.text
            else -> message.text
        }
        val mentionedUserNames = message.mentionedUsers.map { it.name.ifEmpty { it.id } }
        val textColor = textStyle(message.isMine(currentUser), message).color
        val mentionColor = mentionColor(message.isMine(currentUser))
        return buildAnnotatedMessageText(
            text = displayedText,
            textColor = textColor,
            textFontStyle = typography.body.fontStyle,
            linkColor = colors.primaryAccent,
            mentionsColor = mentionColor,
            mentionedUserNames = mentionedUserNames,
            builder = {
                builder?.invoke(this, message, currentUser)
            },
        )
    }
}
