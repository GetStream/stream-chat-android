package io.getstream.chat.android.compose.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.getstream.chat.android.client.models.User

/**
 * Provides sample users that will be used to render user avatar previews.
 */
internal class UserAvatarPreviewParameterProvider : PreviewParameterProvider<User> {
    override val values: Sequence<User> = sequenceOf(
        // Offline user with image
        PreviewUserData.user1,
        // Online user with image
        PreviewUserData.user2.copy(online = true),
        // Offline user without image
        PreviewUserData.user2.apply { image = "" }
    )
}
