package io.getstream.chat.android.ui.common.extensions

import android.content.Context
import com.getstream.sdk.chat.utils.extensions.getUsers
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.R

public fun Channel.getDisplayName(context: Context): String =
    name.takeIf { it.isNotEmpty() }
        ?: getUsers()
            .joinToString { it.name }
            .takeIf { it.isNotEmpty() }
        ?: context.getString(R.string.stream_ui_stream_channel_unknown_title)
