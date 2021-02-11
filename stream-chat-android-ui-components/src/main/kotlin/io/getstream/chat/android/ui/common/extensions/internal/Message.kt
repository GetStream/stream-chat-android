package io.getstream.chat.android.ui.common.extensions.internal

import android.content.Context
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.R

internal fun Message.getSenderDisplayName(context: Context, isDirectMessaging: Boolean = false): String? =
    when {
        user.isCurrentUser() -> getSelfDisplayName(context)
        isDirectMessaging -> null
        else -> user.asMention(context)
    }

private fun getSelfDisplayName(context: Context) = context.getString(R.string.stream_ui_channel_display_name_self)
