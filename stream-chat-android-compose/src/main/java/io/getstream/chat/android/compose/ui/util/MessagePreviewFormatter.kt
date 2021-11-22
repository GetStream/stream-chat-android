package io.getstream.chat.android.compose.ui.util

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 *  An interface that allows to generate a preview text for the given message.
 */
public fun interface MessagePreviewFormatter {

    /**
     * Generates a preview text for the given message.
     *
     * @param message The message whose data is used to generate the preview text.
     * @param currentUser The currently logged in user.
     * @return The formatted text representation for the given message.
     */
    @Composable
    public fun formatMessagePreview(message: Message, currentUser: User?): AnnotatedString

    public companion object {
        /**
         * Builds the default message preview text formatter.
         *
         * @param context The context to load string resources.
         * @return The default implementation of [MessagePreviewFormatter].
         *
         * @see [DefaultMessagePreviewFormatter]
         */
        public fun defaultFormatter(context: Context): MessagePreviewFormatter {
            return DefaultMessagePreviewFormatter(context)
        }
    }
}

/**
 * The default implementation of [MessagePreviewFormatter] that allows to generate a preview text for
 * a message with the following spans: sender name, message text, attachments preview text.
 *
 * @param context The context to load string resources.
 */
private class DefaultMessagePreviewFormatter(
    private val context: Context,
) : MessagePreviewFormatter {

    /**
     * Generates a preview text for the given message.
     *
     * @param message The message whose data is used to generate the preview text.
     * @param currentUser The currently logged in user.
     * @return The formatted text representation for the given message.
     */
    @Composable
    override fun formatMessagePreview(message: Message, currentUser: User?): AnnotatedString {
        return buildAnnotatedString {
            message.let { message ->
                val messageText = message.text.trim()

                if (message.isSystem()) {
                    append(messageText)
                } else {
                    appendSenderName(message, currentUser)
                    appendMessageText(messageText)
                    appendAttachmentText(message.attachments)
                }
            }
        }
    }

    /**
     * Appends the sender name to the [AnnotatedString].
     */
    @SuppressLint("ComposableNaming")
    @Composable
    private fun AnnotatedString.Builder.appendSenderName(message: Message, currentUser: User?) {
        val sender = message.getSenderDisplayName(context, currentUser)

        if (sender != null) {
            append("$sender: ")

            addStyle(
                SpanStyle(
                    fontStyle = ChatTheme.typography.bodyBold.fontStyle
                ),
                start = 0,
                end = sender.length
            )
        }
    }

    /**
     * Appends the message text to the [AnnotatedString].
     */
    @SuppressLint("ComposableNaming")
    @Composable
    private fun AnnotatedString.Builder.appendMessageText(messageText: String) {
        if (messageText.isNotEmpty()) {
            val startIndex = this.length
            append("$messageText ")

            addStyle(
                SpanStyle(
                    fontStyle = ChatTheme.typography.bodyBold.fontStyle
                ),
                start = startIndex,
                end = startIndex + messageText.length
            )
        }
    }

    /**
     * Appends a string representations of [attachments] to the [AnnotatedString].
     */
    @SuppressLint("ComposableNaming")
    @Composable
    private fun AnnotatedString.Builder.appendAttachmentText(attachments: List<Attachment>) {
        if (attachments.isNotEmpty()) {
            ChatTheme.attachmentFactories
                .firstOrNull { it.canHandle(attachments) }
                ?.textFormatter
                ?.let { textFormatter ->
                    attachments.mapNotNull { attachment ->
                        textFormatter.invoke(attachment)
                            .let { previewText ->
                                previewText.ifEmpty { null }
                            }
                    }.joinToString()
                }?.let { attachmentText ->
                    val startIndex = this.length
                    append(attachmentText)

                    addStyle(
                        SpanStyle(
                            fontStyle = ChatTheme.typography.bodyItalic.fontStyle
                        ),
                        start = startIndex,
                        end = startIndex + attachmentText.length
                    )
                }
        }
    }
}
