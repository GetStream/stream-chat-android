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

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import io.getstream.chat.android.client.utils.attachment.isGiphy
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageTheme
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamShapes
import io.getstream.chat.android.compose.ui.theme.StreamTypography
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.utils.extensions.isMine
import io.getstream.chat.android.uiutils.extension.isAnyFileType

/**
 * An interface that allows to format the quoted message text.
 */
public fun interface QuotedMessageTextFormatter {

    /**
     * Formats the given message text.
     *
     * @param message The quoted message to format.
     * @param replyMessage The message that contains the reply.
     * @param currentUser The currently logged in user.
     * @return The formatted message text.
     */
    public fun format(message: Message, replyMessage: Message?, currentUser: User?): AnnotatedString

    public companion object {

        /**
         * Builds the default message text formatter.
         *
         * @param autoTranslationEnabled Whether the auto-translation is enabled.
         * @param context The context to load resources.
         * @param isInDarkMode Whether the app is in dark mode.
         * @param typography The typography to use for styling.
         * @param colors The colors to use for styling.
         * @param textStyle The text style to use for styling.
         * @param mentionColor The color to use for mentions.
         * @param builder The builder to use for customizing the text.
         * @return The default implementation of [QuotedMessageTextFormatter].
         *
         * @see [DefaultQuotedMessageTextFormatter]
         */
        @Composable
        public fun defaultFormatter(
            autoTranslationEnabled: Boolean,
            context: Context = LocalContext.current,
            isInDarkMode: Boolean = isSystemInDarkTheme(),
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isInDarkMode) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
            shapes: StreamShapes = StreamShapes.defaultShapes(),
            textStyle: (isMine: Boolean) -> TextStyle = defaultTextStyle(isInDarkMode, typography, colors, shapes),
            linkStyle: (isMine: Boolean) -> TextStyle = defaultLinkStyle(colors),
            mentionColor: (isMine: Boolean) -> Color = defaultMentionColor(isInDarkMode, typography, colors, shapes),
            builder: AnnotatedQuotedMessageTextBuilder? = null,
        ): QuotedMessageTextFormatter {
            return DefaultQuotedMessageTextFormatter(
                context = context,
                autoTranslationEnabled = autoTranslationEnabled,
                typography = typography,
                textStyle = textStyle,
                linkStyle = linkStyle,
                mentionColor = mentionColor,
                builder = builder,
            )
        }

        /**
         * Builds the default message text formatter.
         *
         * @param autoTranslationEnabled Whether the auto-translation is enabled.
         * @param context The context to load resources.
         * @param isInDarkMode Whether the app is in dark mode.
         * @param typography The typography to use for styling.
         * @param colors The colors to use for styling.
         * @param ownMessageTheme The theme to use for the current user's messages.
         * @param otherMessageTheme The theme to use for other users' messages.
         * @param builder The builder to use for customizing the text.
         * @return The default implementation of [QuotedMessageTextFormatter].
         *
         * @see [DefaultQuotedMessageTextFormatter]
         */
        @Composable
        public fun defaultFormatter(
            autoTranslationEnabled: Boolean,
            context: Context = LocalContext.current,
            isInDarkMode: Boolean = isSystemInDarkTheme(),
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isInDarkMode) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
            shapes: StreamShapes = StreamShapes.defaultShapes(),
            ownMessageTheme: MessageTheme = MessageTheme.defaultOwnTheme(isInDarkMode, typography, shapes, colors),
            otherMessageTheme: MessageTheme = MessageTheme.defaultOtherTheme(isInDarkMode, typography, shapes, colors),
            builder: AnnotatedQuotedMessageTextBuilder? = null,
        ): QuotedMessageTextFormatter {
            val textStyle = defaultTextStyle(ownMessageTheme, otherMessageTheme)
            val linkStyle = defaultLinkStyle(ownMessageTheme, otherMessageTheme)
            val mentionColor = defaultMentionColor(ownMessageTheme, otherMessageTheme)
            return defaultFormatter(
                autoTranslationEnabled = autoTranslationEnabled,
                context = context,
                isInDarkMode = isInDarkMode,
                typography = typography,
                colors = colors,
                shapes = shapes,
                textStyle = textStyle,
                linkStyle = linkStyle,
                mentionColor = mentionColor,
                builder = builder,
            )
        }

        @Composable
        private fun defaultTextStyle(
            isInDarkMode: Boolean,
            typography: StreamTypography,
            colors: StreamColors,
            shapes: StreamShapes,
        ): (Boolean) -> TextStyle {
            val ownTheme = MessageTheme.defaultOwnTheme(isInDarkMode, typography, shapes, colors)
            val otherTheme = MessageTheme.defaultOtherTheme(isInDarkMode, typography, shapes, colors)
            return defaultTextStyle(ownTheme, otherTheme)
        }

        @Composable
        private fun defaultTextStyle(ownTheme: MessageTheme, otherTheme: MessageTheme): (Boolean) -> TextStyle {
            return { isMine ->
                when (isMine) {
                    true -> ownTheme.quoted.textStyle
                    else -> otherTheme.quoted.textStyle
                }
            }
        }

        /**
         * Builds a composite message text formatter.
         *
         * @param formatters The list of formatters to use.
         * @return The composite implementation of [QuotedMessageTextFormatter].
         *
         * @see [CompositeMessageTextFormatter]
         */
        public fun composite(vararg formatters: QuotedMessageTextFormatter): QuotedMessageTextFormatter {
            return CompositeQuotedMessageTextFormatter(formatters.toList())
        }
    }
}

/**
 * A builder for the annotated message text.
 */
public typealias AnnotatedQuotedMessageTextBuilder = AnnotatedString.Builder.(
    message: Message,
    replyMessage: Message?,
    currentUser: User?,
) -> Unit

private class CompositeQuotedMessageTextFormatter(
    private val formatters: List<QuotedMessageTextFormatter>,
) : QuotedMessageTextFormatter {

    override fun format(message: Message, replyMessage: Message?, currentUser: User?): AnnotatedString {
        val builder = AnnotatedString.Builder(message.text)
        for (formatter in formatters) {
            builder.merge(formatter.format(message, replyMessage, currentUser))
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
private class DefaultQuotedMessageTextFormatter(
    private val context: Context,
    private val autoTranslationEnabled: Boolean,
    private val typography: StreamTypography,
    private val textStyle: (isMine: Boolean) -> TextStyle,
    private val linkStyle: (isMine: Boolean) -> TextStyle,
    private val mentionColor: (isMine: Boolean) -> Color,
    private val builder: AnnotatedQuotedMessageTextBuilder? = null,
) : QuotedMessageTextFormatter {

    override fun format(message: Message, replyMessage: Message?, currentUser: User?): AnnotatedString {
        val displayedText = when (autoTranslationEnabled) {
            true -> currentUser?.language?.let { userLanguage ->
                message.getTranslation(userLanguage).ifEmpty { message.text }
            } ?: message.text

            else -> message.text
        }

        val poll = message.poll
        val sharedLocation = message.sharedLocation
        val attachment = message.attachments.firstOrNull()
        val quotedMessageText = when {
            message.isDeleted() -> context.getString(R.string.stream_ui_message_list_message_deleted)
            poll != null -> context.getString(R.string.stream_compose_quoted_message_poll, poll.name)
            sharedLocation != null -> context.getString(sharedLocation.getMessageTextResId())
            displayedText.isNotBlank() -> displayedText
            attachment != null -> when {
                attachment.name != null -> attachment.name
                attachment.text != null -> attachment.text
                attachment.isImage() -> context.getString(R.string.stream_compose_quoted_message_image_tag)
                attachment.isGiphy() -> context.getString(R.string.stream_compose_quoted_message_giphy_tag)
                attachment.isAnyFileType() -> context.getString(R.string.stream_compose_quoted_message_file_tag)
                else -> displayedText
            }

            else -> displayedText
        }

        checkNotNull(quotedMessageText) {
            "quotedMessageText is null. Cannot display invalid message title."
        }

        val isMine = message.isMine(currentUser)
        val textColor = textStyle(isMine).color
        val linkStyle = linkStyle(isMine)
        val mentionColor = mentionColor(isMine)
        return buildAnnotatedMessageText(
            text = quotedMessageText,
            textColor = textColor,
            textFontStyle = typography.body.fontStyle,
            linkStyle = linkStyle,
            mentionsColor = mentionColor,
            builder = {
                builder?.invoke(this, message, replyMessage, currentUser)
            },
        )
    }
}
