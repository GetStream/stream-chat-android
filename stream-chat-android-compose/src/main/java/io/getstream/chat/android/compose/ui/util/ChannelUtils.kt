package io.getstream.chat.android.compose.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import com.getstream.sdk.chat.utils.extensions.getUsers
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.compose.R

@Composable
@ReadOnlyComposable
public fun Channel.getDisplayName(): String {
    return name.takeIf { it.isNotEmpty() }
        ?: getUsers()
            .joinToString { it.name }
            .takeIf { it.isNotEmpty() }
        ?: stringResource(id = R.string.stream_compose_channel_list_untitled_channel)
}
