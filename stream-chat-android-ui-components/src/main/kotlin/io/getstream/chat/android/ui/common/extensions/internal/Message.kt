package io.getstream.chat.android.ui.common.extensions.internal

import android.content.Context
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.R

internal fun Message.getSenderDisplayName(context: Context, isDirectMessaging: Boolean = false): String? =
    when {
        user.isCurrentUser() -> context.getString(R.string.stream_ui_channel_list_you)
        isDirectMessaging -> null
        else -> user.asMention(context)
    }

internal fun Message.getPinnedText(context: Context): String {
    val name = when {
        user.isCurrentUser() -> context.getString(R.string.stream_ui_message_list_pinned_message_by_you)
        else -> pinnedBy?.name ?: ""
    }
    return context.getString(R.string.stream_ui_message_list_pinned_message, name)
}
