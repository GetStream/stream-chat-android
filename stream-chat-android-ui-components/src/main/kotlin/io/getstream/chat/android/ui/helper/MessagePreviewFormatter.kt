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

package io.getstream.chat.android.ui.helper

import android.content.Context
import android.text.SpannableString
import android.text.SpannableStringBuilder
import io.getstream.chat.android.client.utils.message.hasAudioRecording
import io.getstream.chat.android.client.utils.message.isSystem
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.singletonList
import io.getstream.chat.android.ui.common.utils.extensions.isDirectMessaging
import io.getstream.chat.android.ui.utils.extensions.asMention
import io.getstream.chat.android.ui.utils.extensions.bold
import io.getstream.chat.android.ui.utils.extensions.getAttachmentsText
import io.getstream.chat.android.ui.utils.extensions.getSenderDisplayName
import io.getstream.chat.android.ui.utils.extensions.getTranslatedText
import io.getstream.chat.android.ui.utils.extensions.italicize

/**
 * An interface that allows to generate a preview text for the given message.
 */
public fun interface MessagePreviewFormatter {

    /**
     * Generates a preview text for the given message.
     *
     * @param channel The channel containing the message.
     * @param message The message whose data is used to generate the preview text.
     * @param currentUser The currently logged in user.
     * @return The formatted text representation for the given message.
     */
    public fun formatMessagePreview(
        channel: Channel,
        message: Message,
        currentUser: User?,
    ): CharSequence

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
            return DefaultMessagePreviewFormatter(context = context)
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
     * @param channel The channel containing the message.
     * @param message The message whose data is used to generate the preview text.
     * @param currentUser The currently logged in user.
     * @return The formatted text representation for the given message.
     */
    override fun formatMessagePreview(
        channel: Channel,
        message: Message,
        currentUser: User?,
    ): CharSequence {
        val displayedText = message.getTranslatedText(currentUser)
        return if (message.isSystem()) {
            SpannableStringBuilder(displayedText.trim().italicize())
        } else {
            val sender = message.getSenderDisplayName(context, channel.isDirectMessaging())

            if (message.hasAudioRecording()) {
                val voiceText = context.getString(R.string.stream_ui_message_audio_reply_info).italicize()
                listOf(sender, voiceText)
                    .filterNot { it.isNullOrEmpty() }
                    .joinTo(SpannableStringBuilder(), ": ")
            } else {
                // bold mentions of the current user
                val currentUserMention = currentUser?.asMention(context)
                val previewText: SpannableString =
                    displayedText.trim().bold(currentUserMention?.singletonList(), ignoreCase = true)

                val attachmentsText: SpannableString? = message.getAttachmentsText()

                listOf(sender, previewText, attachmentsText)
                    .filterNot { it.isNullOrEmpty() }
                    .joinTo(SpannableStringBuilder(), ": ")
            }
        }
    }
}
