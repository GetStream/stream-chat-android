package io.getstream.chat.android.compose.ui.util

import android.content.Context
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.R

/**
 * Takes the current message and returns the sender display name.
 *
 * @return Sender display name.
 */
internal fun Message.getSenderDisplayName(
    context: Context,
    currentUser: User?,
): String? =
    when (user.id) {
        currentUser?.id -> context.getString(R.string.stream_compose_channel_list_you)
        else -> null
    }

/**
 * @return If the message type is regular.
 */
internal fun Message.isRegular(): Boolean = type == ModelType.message_regular

/**
 * @return If the message type is ephemeral.
 */
internal fun Message.isEphemeral(): Boolean = type == ModelType.message_ephemeral

/**
 * @return If the message type is system.
 */
internal fun Message.isSystem(): Boolean = type == ModelType.message_system

/**
 * @return If the message type is error.
 */
internal fun Message.isError(): Boolean = type == ModelType.message_error

/**
 * @return If the message is deleted.
 */
internal fun Message.isDeleted(): Boolean = deletedAt != null

/**
 * @return If the message contains an attachment that is currently being uploaded.
 */
internal fun Message.isUploading(): Boolean = attachments.any { it.isUploading() }

/**
 * @return If the message is a start of a thread.
 */
internal fun Message.hasThread(): Boolean = threadParticipants.isNotEmpty()

/**
 * @return If the message is related to a Giphy slash command.
 */
internal fun Message.isGiphy(): Boolean = command == ModelType.attach_giphy

/**
 * @return If the message is a temporary message to select a gif.
 */
internal fun Message.isGiphyEphemeral(): Boolean = isGiphy() && isEphemeral()
