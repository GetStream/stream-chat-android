// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.general

import android.os.Bundle
import android.text.format.DateUtils
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamShapes
import io.getstream.chat.android.compose.ui.theme.StreamTypography
import io.getstream.chat.android.compose.ui.util.MessagePreviewFormatter
import io.getstream.chat.android.compose.ui.util.MessageTextFormatter
import io.getstream.chat.android.compose.ui.util.QuotedMessageTextFormatter
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.helper.DateFormatter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

abstract class ChatThemeCustomization : AppCompatActivity() {

        protected lateinit var viewModelFactory: MessagesViewModelFactory

        protected val autoTranslationEnabled = true

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent(content = content)
        }

        protected abstract val content: @Composable () -> Unit
}

/**
 * [Usage](https://getstream.io/chat/docs/sdk/android/compose/general-customization/chat-theme/#usage)
 */
private object ChatThemeUsageSnippet {

    class MessageListActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    MessagesScreen(
                        viewModelFactory = MessagesViewModelFactory(
                            context = this,
                            channelId = "messaging:123",
                            messageLimit = 30
                        ),
                        onBackPressed = { finish() },
                        onHeaderTitleClick = {},
                    )
                }
            }
        }
    }
}

/**
 * [Customization](https://getstream.io/chat/docs/sdk/android/compose/general-customization/chat-theme/#customization)
 */
private object ChatThemeCustomizationSnippet {

    class MyActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme(
                    shapes = StreamShapes.defaultShapes().copy( // Customizing the shapes
                        avatar = RoundedCornerShape(8.dp),
                        attachment = RoundedCornerShape(16.dp),
                        inputField = RectangleShape,
                        myMessageBubble = RoundedCornerShape(16.dp),
                        otherMessageBubble = RoundedCornerShape(16.dp),
                        bottomSheet = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                ) {
                    MessagesScreen(
                        viewModelFactory = MessagesViewModelFactory(
                            context = this,
                            channelId = "messaging:123",
                        ),
                        onBackPressed = { finish() },
                        onHeaderTitleClick = {},
                    )
                }
            }
        }
    }
}

private object ChatThemeDateFormatterSnippet : ChatThemeCustomization() {
    override val content: @Composable () -> Unit get() = {
        ChatTheme(
            dateFormatter = buildDateFormatter()
        ) {
            MessagesScreen(
                viewModelFactory = viewModelFactory,
                onBackPressed = { finish() },
            )
        }
    }

    private fun buildDateFormatter(): DateFormatter {
        return object : DateFormatter {
            private val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy")
            private val timeFormat: DateFormat = SimpleDateFormat("HH:mm")

            override fun formatDate(date: Date?): String {
                date ?: return ""
                return dateFormat.format(date)
            }

            override fun formatTime(date: Date?): String {
                date ?: return ""
                return timeFormat.format(date)
            }

            override fun formatRelativeTime(date: Date?): String {
                date ?: return ""
                return DateUtils.getRelativeDateTimeString(
                    applicationContext,
                    date.time,
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.WEEK_IN_MILLIS,
                    0
                ).toString()
            }
        }
    }
}

private object ChatThemeMessageTextFormatterDefaultSnippet : ChatThemeCustomization() {

    override val content: @Composable () -> Unit get() = {
        val colors = if (isSystemInDarkTheme()) StreamColors.defaultDarkColors() else StreamColors.defaultColors()
        val typography = StreamTypography.defaultTypography()
        ChatTheme(
            colors = colors,
            typography = typography,
            messageTextFormatter = buildMessageTextFormatter(typography, colors)
        ) {
            MessagesScreen(
                viewModelFactory = viewModelFactory,
                onBackPressed = { finish() },
            )
        }
    }

    /**
     * Builds default [MessageTextFormatter] with extended functionality, which
     * adds a blue color to the first 3 letters of the  message text.
     */
    @Composable
    private fun buildMessageTextFormatter(
        typography: StreamTypography,
        colors: StreamColors,
    ): MessageTextFormatter {
        val formatter = object : MessageTextFormatter {
            override fun format(message: Message, currentUser: User?): AnnotatedString {
                return buildAnnotatedString {
                    append(message.text)
                    // add your custom styling here
                }
            }
        }

        val quotedFormatter = object : QuotedMessageTextFormatter {
            override fun format(message: Message, replyMessage: Message?, currentUser: User?): AnnotatedString {
                return buildAnnotatedString {
                    append(message.text)
                    // add your custom styling here
                }
            }
        }

        val previewFormatter = object : MessagePreviewFormatter {
            override fun formatMessagePreview(message: Message, currentUser: User?): AnnotatedString {
                return buildAnnotatedString {
                    append(message.text)
                    // add your custom styling here
                }
            }
        }
        return MessageTextFormatter.defaultFormatter(
            autoTranslationEnabled = autoTranslationEnabled,
            typography = typography,
            colors = colors,
        ) { message, currentUser ->
            addStyle(
                SpanStyle(
                    color = Color.Blue,
                ),
                start = 0,
                end = minOf(3, length),
            )
        }
    }
}

private object ChatThemeMessageTextFormatterCompositeSnippet : ChatThemeCustomization() {
    override val content: @Composable () -> Unit get() = {
        val colors = if (isSystemInDarkTheme()) StreamColors.defaultDarkColors() else StreamColors.defaultColors()
        val typography = StreamTypography.defaultTypography()
        ChatTheme(
            colors = colors,
            typography = typography,
            messageTextFormatter = buildMessageTextFormatter(typography, colors)
        ) {
            MessagesScreen(
                viewModelFactory = viewModelFactory,
                onBackPressed = { finish() },
            )
        }
    }


    @Composable
    private fun buildMessageTextFormatter(
        typography: StreamTypography,
        colors: StreamColors,
    ): MessageTextFormatter {
        return MessageTextFormatter.composite(
            MessageTextFormatter.defaultFormatter(
                autoTranslationEnabled = autoTranslationEnabled,
                typography = typography,
                colors = colors
            ),
            blueLettersMessageTextFormatter()
        )
    }

    /**
     * Builds a [MessageTextFormatter] that adds a blue color to the first 3 letters of the message text.
     */
    @Composable
    private fun blueLettersMessageTextFormatter(): MessageTextFormatter {
        return MessageTextFormatter { message, currentUser ->
            buildAnnotatedString {
                append(message.text)
                addStyle(
                    SpanStyle(
                        color = Color.Blue,
                    ),
                    start = 0,
                    end = minOf(3, message.text.length),
                )
            }
        }
    }
}

private object ChatThemeQuotedMessageTextFormatterDefaultSnippet : ChatThemeCustomization() {

    override val content: @Composable () -> Unit get() = {
        val isInDarkMode: Boolean = isSystemInDarkTheme()
        val colors = if (isInDarkMode) StreamColors.defaultDarkColors() else StreamColors.defaultColors()
        val typography = StreamTypography.defaultTypography()
        val defaultQuotedTextFormatter = buildQuotedMessageTextFormatter(isInDarkMode, typography, colors)
        ChatTheme(
            colors = colors,
            typography = typography,
            quotedMessageTextFormatter = defaultQuotedTextFormatter
        ) {
            MessagesScreen(
                viewModelFactory = viewModelFactory,
                onBackPressed = { finish() },
            )
        }
    }

    /**
     * Builds default [QuotedMessageTextFormatter] with extended functionality, which
     * adds a blue color to the first 3 letters of the quoted message text.
     */
    @Composable
    private fun buildQuotedMessageTextFormatter(
        isInDarkMode: Boolean,
        typography: StreamTypography,
        colors: StreamColors,
    ): QuotedMessageTextFormatter {
        return QuotedMessageTextFormatter.defaultFormatter(
            autoTranslationEnabled, LocalContext.current, isInDarkMode, typography, colors
        ) { message, replyMessage, currentUser ->
            addStyle(
                SpanStyle(
                    color = Color.Blue,
                ),
                start = 0,
                end = minOf(3, length),
            )
        }
    }
}

private object ChatThemeQuotedMessageTextFormatterCompositeSnippet : ChatThemeCustomization() {
    override val content: @Composable () -> Unit get() = {
        val isInDarkMode = isSystemInDarkTheme()
        val colors = if (isInDarkMode) StreamColors.defaultDarkColors() else StreamColors.defaultColors()
        val typography = StreamTypography.defaultTypography()
        ChatTheme(
            colors = colors,
            typography = typography,
            quotedMessageTextFormatter = buildQuotedMessageTextFormatter(isInDarkMode, typography, colors)
        ) {
            MessagesScreen(
                viewModelFactory = viewModelFactory,
                onBackPressed = { finish() },
            )
        }
    }


    @Composable
    private fun buildQuotedMessageTextFormatter(
        isInDarkMode: Boolean,
        typography: StreamTypography,
        colors: StreamColors,
    ): QuotedMessageTextFormatter {
        return QuotedMessageTextFormatter.composite(
            QuotedMessageTextFormatter.defaultFormatter(
                autoTranslationEnabled,
                LocalContext.current,
                isInDarkMode,
                typography,
                colors
            ),
            blueLettersQuotedMessageTextFormatter()
        )
    }

    /**
     * Builds a [QuotedMessageTextFormatter] that adds a blue color to the first 3 letters of the quoted message text.
     */
    @Composable
    private fun blueLettersQuotedMessageTextFormatter(): QuotedMessageTextFormatter {
        return QuotedMessageTextFormatter { message, replyMessage, currentUser ->
            buildAnnotatedString {
                append(message.text)
                addStyle(
                    SpanStyle(
                        color = Color.Blue,
                    ),
                    start = 0,
                    end = minOf(3, message.text.length),
                )
            }
        }
    }
}