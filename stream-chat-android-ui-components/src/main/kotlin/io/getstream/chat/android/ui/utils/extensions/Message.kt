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

package io.getstream.chat.android.ui.utils.extensions

import android.content.Context
import android.text.SpannableString
import io.getstream.chat.android.client.utils.message.isEphemeral
import io.getstream.chat.android.client.utils.message.isError
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.ReactionGroup
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.helper.CurrentUserProvider
import java.util.Date

@Deprecated(
    message = "Use the `isErrorOrFailed()` from `stream-chat-android-client` module instead.",
    replaceWith = ReplaceWith(
        expression = "Message.isErrorOrFailed()",
        imports = ["io.getstream.chat.android.client.utils.message.isErrorOrFailed"],
    ),
    level = DeprecationLevel.WARNING,
)
public fun Message.isFailed(): Boolean {
    return this.syncStatus == SyncStatus.FAILED_PERMANENTLY || isError()
}

public fun Message.hasNoAttachments(): Boolean = attachments.isEmpty()

public fun Message.isGiphyNotEphemeral(): Boolean = isEphemeral().not() && command == AttachmentType.GIPHY

public fun Message.getCreatedAtOrNull(): Date? = createdAt ?: createdLocallyAt

public fun Message.getUpdatedAtOrNull(): Date? = updatedAt ?: updatedLocallyAt

public fun Message.getCreatedAtOrThrow(): Date = checkNotNull(getCreatedAtOrNull()) {
    "a message needs to have a non null value for either createdAt or createdLocallyAt"
}

public fun Message.hasSingleReaction(): Boolean {
    return supportedReactionGroups.size == 1
}

public fun Message.hasReactions(): Boolean {
    return supportedReactionGroups.isNotEmpty()
}

public val Message.supportedLatestReactions: List<Reaction>
    get() {
        return if (latestReactions.isEmpty()) {
            latestReactions
        } else {
            latestReactions.filter { ChatUI.supportedReactions.isReactionTypeSupported(it.type) }
        }
    }

public val Message.supportedReactionGroups: Map<String, ReactionGroup>
    get() = reactionGroups.filterKeys { ChatUI.supportedReactions.isReactionTypeSupported(it) }

public val Message.supportedReactionCounts: Map<String, Int>
    get() {
        return if (reactionCounts.isEmpty()) {
            reactionCounts
        } else {
            reactionCounts.filterKeys { ChatUI.supportedReactions.isReactionTypeSupported(it) }
        }
    }

public fun Message.hasText(): Boolean = text.isNotEmpty()

internal fun Message.getSenderDisplayName(context: Context, isDirectMessaging: Boolean = false): String? =
    when {
        user.isCurrentUser() -> context.getString(R.string.stream_ui_channel_list_you)
        isDirectMessaging -> null
        else -> user.asMention(context)
    }

internal fun Message.getPinnedText(context: Context): String? {
    val pinnedBy = pinnedBy ?: return null

    val user = if (pinnedBy.isCurrentUser()) {
        context.getString(R.string.stream_ui_message_list_pinned_message_you)
    } else {
        pinnedBy.name
    }
    return context.getString(R.string.stream_ui_message_list_pinned_message, user)
}

/**
 * Returns a string representation of message attachments or null if the attachment list is empty.
 */
internal fun Message.getAttachmentsText(): SpannableString? {
    return attachments.takeIf { it.isNotEmpty() }
        ?.mapNotNull { attachment ->
            attachment.title?.let { title ->
                val prefix = getAttachmentPrefix(attachment)
                if (prefix != null) {
                    "$prefix $title"
                } else {
                    title
                }
            } ?: attachment.name ?: attachment.fallback
        }
        ?.joinToString()
        ?.italicize()
}

private fun getAttachmentPrefix(attachment: Attachment): String? =
    when (attachment.type) {
        AttachmentType.GIPHY -> "/giphy"
        else -> null
    }

internal fun Message.getTranslatedText(currentUser: User?): String {
    return getTranslatedText { currentUser }
}

internal inline fun Message.getTranslatedText(getCurrentUser: () -> User?): String {
    return when (ChatUI.autoTranslationEnabled) {
        true -> getCurrentUser()?.language?.let { userLanguage ->
            getTranslation(userLanguage).ifEmpty { text }
        } ?: text
        else -> text
    }
}

internal fun Message.getTranslatedText(currentUserProvider: CurrentUserProvider = ChatUI.currentUserProvider): String {
    return getTranslatedText(currentUserProvider::getCurrentUser)
}

/**
 * Returns the appropriate message text based on auto-translation settings and the user's preference.
 *
 * If auto-translation is enabled:
 * - Returns the original text if [showOriginalText] is true.
 * - Otherwise, returns the translated text for the current user's language, or the original text if no translation
 * is available or the user has no language set.
 *
 * If auto-translation is disabled, always returns the original message text.
 *
 * @param showOriginalText Whether to show the original message text instead of the translation.
 * @param currentUserProvider Provider for the current user, used to determine the preferred language for translation.
 * @return The text to display for the message, either original or translated.
 */
internal fun Message.getToggleableTranslatedText(
    showOriginalText: Boolean,
    currentUserProvider: CurrentUserProvider = ChatUI.currentUserProvider,
): String {
    return when (ChatUI.autoTranslationEnabled) {
        true -> {
            // If auto-translation is enabled, we check if the message is showing original text.
            // If it is, we return the original text, otherwise we return the translated text.
            if (showOriginalText) {
                text
            } else {
                // If the message is not showing original text, we check if the current user has a language set.
                // If they do, we return the translated text, otherwise we return the original text.
                currentUserProvider.getCurrentUser()?.language?.let { userLanguage ->
                    getTranslation(userLanguage).ifEmpty { text }
                } ?: text
            }
        }
        else -> text
    }
}
