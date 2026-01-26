// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.utility

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.components.avatar.ChannelAvatar
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User

/**
 * [Usage](https://getstream.io/chat/docs/sdk/android/compose/utility-components/channel-avatar/#usage)
 */
private object ChannelAvatarUsageSnippet {

    @Composable
    fun MyChannelAvatar(channel: Channel, currentUser: User) {
        ChannelAvatar(
            channel = channel,
            // The current logged in user
            currentUser = currentUser,
            // Reasonable avatar size
            modifier = Modifier.size(36.dp)
        )
    }
}

/**
 * [Handling Actions](https://getstream.io/chat/docs/sdk/android/compose/utility-components/channel-avatar/#handling-actions)
 */
private object ChannelAvatarHandlingActionsSnippet {

    @Composable
    fun MyChannelAvatar(channel: Channel, currentUser: User) {
        ChannelAvatar(
            channel = channel,
            currentUser = currentUser,
            modifier = Modifier.clickable {
                // Handle avatar clicks here
            }
        )
    }
}

/**
 * [Customization](https://getstream.io/chat/docs/sdk/android/compose/utility-components/channel-avatar/#customization)
 */
private object ChannelAvatarCustomizationSnippet {

    @Composable
    fun MyChannelAvatar(channel: Channel, currentUser: User) {
        ChannelAvatar(
            channel = channel,
            currentUser = currentUser,
            modifier = Modifier.size(48.dp),
        )
    }
}
