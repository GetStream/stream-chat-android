package io.getstream.chat.android.ui.common.extensions.internal

import android.content.Context
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.R

internal fun Message.getSenderDisplayName(context: Context, isDirectMessaging: Boolean = false): String? =
    when {
        user.isCurrentUser() -> context.getString(R.string.stream_ui_channel_list_you)
        isDirectMessaging -> null
        else -> user.asMention(context)
    }
