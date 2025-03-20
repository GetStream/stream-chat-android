/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import io.getstream.chat.android.client.utils.message.hasAudioRecording
import io.getstream.chat.android.client.utils.message.isPoll
import io.getstream.chat.android.client.utils.message.isPollClosed
import io.getstream.chat.android.client.utils.message.isSystem
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamTypography
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User

/**
 * An interface that allows to generate a preview text for the given message.
 */
public interface MessagePreviewFormatter {

    /**
     * Generates a preview title for the given message.
     *
     * @param message The message whose data is used to generate the preview title.
     * @return The formatted text representation of the preview title.
     */
    public fun formatMessageTitle(message: Message): AnnotatedString

    /**
     * Generates a preview text for the given message.
     *
     * @param message The message whose data is used to generate the preview text.
     * @param currentUser The currently logged in user.
     * @return The formatted text representation for the given message.
     */
    public fun formatMessagePreview(message: Message, currentUser: User?): AnnotatedString

    /**
     * Generates a preview text for the given draft message.
     * This is used to show a preview of the draft message in the the channel list.
     *
     * @param draftMessage The draft message whose data is used to generate the preview text.
     * @return The formatted text representation for the given draft message.
     */
    public fun formatDraftMessagePreview(draftMessage: DraftMessage): AnnotatedString

    public companion object {
        /**
         * Builds the default message preview text formatter.
         *
         * @param context The context to load string resources.
         * @param autoTranslationEnabled Whether the auto-translation is enabled.
         * @param typography The typography to use for styling.
         * @param attachmentFactories The list of [AttachmentFactory] to use for formatting attachments.
         * @return The default implementation of [MessagePreviewFormatter].
         *
         * @see [DefaultMessagePreviewFormatter]
         */
        public fun defaultFormatter(
            context: Context,
            autoTranslationEnabled: Boolean,
            typography: StreamTypography,
            attachmentFactories: List<AttachmentFactory>,
            colors: StreamColors,
        ): MessagePreviewFormatter {
            return DefaultMessagePreviewFormatter(
                context = context,
                autoTranslationEnabled = autoTranslationEnabled,
                draftMessageLabelTextStyle = typography.footnoteBold.copy(color = colors.primaryAccent),
                messageTextStyle = typography.bodyBold,
                senderNameTextStyle = typography.bodyBold,
                attachmentTextFontStyle = typography.bodyItalic,
                attachmentFactories = attachmentFactories,
            )
        }
    }
}

/**
 * The default implementation of [MessagePreviewFormatter] that allows to generate a preview text for
 * a message with the following spans: sender name, message text, attachments preview text.
 *
 * @param context The context to load string resources.
 */
@Suppress("LongParameterList")
private class DefaultMessagePreviewFormatter(
    private val context: Context,
    private val autoTranslationEnabled: Boolean,
    private val draftMessageLabelTextStyle: TextStyle,
    private val messageTextStyle: TextStyle,
    private val senderNameTextStyle: TextStyle,
    private val attachmentTextFontStyle: TextStyle,
    private val attachmentFactories: List<AttachmentFactory>,
) : MessagePreviewFormatter {

    private companion object {
        private const val SPACE = " "
    }

    /**
     * Generates a preview title for the given message.
     *
     * @param message The message whose data is used to generate the preview title.
     * @return The formatted text representation of the preview title.
     */
    override fun formatMessageTitle(message: Message): AnnotatedString {
        val channel = message.channelInfo
        return if (channel?.name != null && channel.memberCount > 2) {
            context.getString(
                R.string.stream_compose_message_preview_sender,
                message.user.name,
                channel.name,
            ).parseBoldTags()
        } else {
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(message.user.name)
                }
            }
        }
    }

    /**
     * Generates a preview text for the given message.
     *
     * @param message The message whose data is used to generate the preview text.
     * @param currentUser The currently logged in user.
     * @return The formatted text representation for the given message.
     */
    override fun formatMessagePreview(
        message: Message,
        currentUser: User?,
    ): AnnotatedString {
        return buildAnnotatedString {
            message.let { message ->
                val displayedText = when (autoTranslationEnabled) {
                    true -> currentUser?.language?.let { userLanguage ->
                        message.getTranslation(userLanguage).ifEmpty { message.text }
                    } ?: message.text
                    else -> message.text
                }.trim()

                if (message.isSystem()) {
                    append(displayedText)
                } else if (message.isPoll()) {
                    if (message.isPollClosed()) {
                        append(
                            context.getString(
                                R.string.stream_compose_poll_closed_preview,
                                message.poll?.name.orEmpty(),
                            ),
                        )
                    } else {
                        append(
                            context.getString(
                                R.string.stream_compose_poll_created_preview,
                                message.poll?.name.orEmpty(),
                            ),
                        )
                    }
                } else if (message.hasAudioRecording()) {
                    appendInlineContent(DefaultMessagePreviewIconFactory.VOICE_MESSAGE)
                    append(SPACE)
                    append(context.getString(R.string.stream_compose_audio_recording_preview))
                } else {
                    appendSenderName(
                        message = message,
                        currentUser = currentUser,
                        senderNameTextStyle = senderNameTextStyle,
                    )
                    appendMessageText(
                        messageText = displayedText,
                        messageTextStyle = messageTextStyle,
                    )
                    appendAttachmentText(
                        attachments = message.attachments,
                        attachmentFactories = attachmentFactories,
                        attachmentTextStyle = attachmentTextFontStyle,
                    )
                }
            }
        }
    }

    /**
     * Generates a preview text for the given draft message.
     * This is used to show a preview of the draft message in the the channel list.
     *
     * @param draftMessage The draft message whose data is used to generate the preview text.
     * @return The formatted text representation for the given draft message.
     */
    override fun formatDraftMessagePreview(draftMessage: DraftMessage): AnnotatedString = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                fontStyle = draftMessageLabelTextStyle.fontStyle,
                fontFamily = draftMessageLabelTextStyle.fontFamily,
                color = draftMessageLabelTextStyle.color,
            ),
        ) {
            append(context.getString(R.string.stream_compose_channel_list_draft))
        }
        append(SPACE)
        withStyle(
            style = SpanStyle(
                fontStyle = messageTextStyle.fontStyle,
                fontFamily = messageTextStyle.fontFamily,
                color = messageTextStyle.color,
            ),
        ) {
            append(draftMessage.text)
        }
    }

    /**
     * Appends the sender name to the [AnnotatedString].
     */
    private fun AnnotatedString.Builder.appendSenderName(
        message: Message,
        currentUser: User?,
        senderNameTextStyle: TextStyle,
    ) {
        val sender = message.getSenderDisplayName(context, currentUser)

        if (sender != null) {
            append("$sender: ")

            addStyle(
                SpanStyle(
                    fontStyle = senderNameTextStyle.fontStyle,
                    fontWeight = senderNameTextStyle.fontWeight,
                    fontFamily = senderNameTextStyle.fontFamily,
                ),
                start = 0,
                end = sender.length,
            )
        }
    }

    /**
     * Appends the message text to the [AnnotatedString].
     */
    private fun AnnotatedString.Builder.appendMessageText(
        messageText: String,
        messageTextStyle: TextStyle,
    ) {
        if (messageText.isNotEmpty()) {
            val startIndex = this.length
            append("$messageText ")

            addStyle(
                SpanStyle(
                    fontStyle = messageTextStyle.fontStyle,
                    fontFamily = messageTextStyle.fontFamily,
                ),
                start = startIndex,
                end = startIndex + messageText.length,
            )
        }
    }

    /**
     * Appends a string representations of [attachments] to the [AnnotatedString].
     */
    private fun AnnotatedString.Builder.appendAttachmentText(
        attachments: List<Attachment>,
        attachmentFactories: List<AttachmentFactory>,
        attachmentTextStyle: TextStyle,
    ) {
        if (attachments.isNotEmpty()) {
            attachmentFactories
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
                            fontStyle = attachmentTextStyle.fontStyle,
                            fontFamily = attachmentTextStyle.fontFamily,
                        ),
                        start = startIndex,
                        end = startIndex + attachmentText.length,
                    )
                }
        }
    }
}
