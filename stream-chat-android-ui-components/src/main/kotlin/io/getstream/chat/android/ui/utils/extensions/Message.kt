package io.getstream.chat.android.ui.utils.extensions

import android.content.Context
import android.text.Spanned
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.R

internal fun Message.getSenderDisplayName(context: Context): String =
    when {
        user.isCurrentUser() -> context.getString(R.string.stream_channel_display_name_self)
        else -> context.getString(R.string.stream_mention_user_name_template, user.name)
    }

internal fun Message.getPreviewText(context: Context): Spanned =
    context
        .getString(
            R.string.stream_channel_item_last_message_template,
            getSenderDisplayName(context),
            text
        )
        .boldMentions()
