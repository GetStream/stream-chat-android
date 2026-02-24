// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.general

import android.os.Bundle
import android.text.format.DateUtils
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamTypography
import io.getstream.chat.android.compose.ui.util.MessagePreviewFormatter
import io.getstream.chat.android.compose.ui.util.MessageTextFormatter
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.models.DraftMessage
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
                ChatTheme {
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

            override fun formatRelativeDate(date: Date): String {
                // Provide a way to format Relative Date
                return DateUtils.getRelativeTimeSpanString(
                    date.time,
                    System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE,
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

        val previewFormatter = object : MessagePreviewFormatter {
            override fun formatMessageTitle(message: Message): AnnotatedString {
                return buildAnnotatedString {
                    append(message.user.name)
                    // add your custom styling here
                }
            }

            override fun formatMessagePreview(message: Message, currentUser: User?): AnnotatedString {
                return buildAnnotatedString {
                    append(message.text)
                    // add your custom styling here
                }
            }

            override fun formatDraftMessagePreview(draftMessage: DraftMessage): AnnotatedString {
                return buildAnnotatedString {
                    append(draftMessage.text)
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
                colors = colors,
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
