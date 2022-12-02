// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.utility

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.models.User
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar

/**
 * [Usage](https://getstream.io/chat/docs/sdk/android/compose/utility-components/user-avatar/#usage)
 */
private object UserAvatarUsageSnippet {

    @Composable
    fun MyUserAvatar(user: User) {
        UserAvatar(
            user = user,
            // Show online indicator
            showOnlineIndicator = true,
            // Reasonable avatar size
            modifier = Modifier.size(36.dp)
        )
    }
}

/**
 * [Handling Actions](https://getstream.io/chat/docs/sdk/android/compose/utility-components/user-avatar/#handling-actions)
 */
private object UserAvatarHandlingActionsSnippet {

    @Composable
    fun MyUserAvatar(user: User) {
        UserAvatar(user = user) {
            // Handle avatar clicks here
        }
    }
}

/**
 * [Customization](https://getstream.io/chat/docs/sdk/android/compose/utility-components/user-avatar/#customization)
 */
private object UserAvatarCustomizationSnippet {

    @Composable
    fun MyUserAvatar(user: User) {
        UserAvatar(
            modifier = Modifier.size(48.dp),
            user = user,
            onlineIndicator = {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(12.dp)
                        .background(Color.Blue, CircleShape)
                )
            }
        )
    }
}
