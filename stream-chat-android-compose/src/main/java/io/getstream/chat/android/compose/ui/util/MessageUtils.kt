package io.getstream.chat.android.compose.ui.util

import android.content.Context
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.R

/**
 * Takes the current message and returns the sender display name.
 *
 * @return - Sender display name.
 * */
internal fun Message.getSenderDisplayName(
    context: Context,
    currentUser: User?,
): String? =
    when (user.id) {
        currentUser?.id -> context.getString(R.string.stream_compose_channel_list_you)
        else -> null
    }

/**
 * @return - If the message type is regular.
 * */
public fun Message.isRegular(): Boolean = type == ModelType.message_regular

/**
 * @return - If the message type is system.
 * */
public fun Message.isSystem(): Boolean = type == ModelType.message_system
