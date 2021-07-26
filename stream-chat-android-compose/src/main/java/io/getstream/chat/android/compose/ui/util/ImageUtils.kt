package io.getstream.chat.android.compose.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import coil.compose.rememberImagePainter
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.image

@Composable
internal fun rememberChannelImagePainter(channel: Channel, currentUser: User?): Painter {

    // TODO - We need to polish this to match the core SDK behavior
    return rememberImagePainter(
        data = when {
            channel.image.isNotEmpty() -> channel.image
            channel.members.size == 2 -> channel.members.first { it.getUserId() != currentUser?.id }.user.image
            else -> ""
        }
    )
}
