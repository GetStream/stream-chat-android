package io.getstream.chat.android.compose.previewdata

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.ui.common.avatar.UserAvatar

/**
 * Provides sample users that will be used to render previews for the [UserAvatar] component.
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
