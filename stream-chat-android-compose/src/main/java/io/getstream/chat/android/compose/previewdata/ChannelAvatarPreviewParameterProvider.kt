package io.getstream.chat.android.compose.previewdata

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.ui.common.avatar.ChannelAvatar

/**
 * Provides sample channels that will be used to render previews for the [ChannelAvatar] component.
 */
internal class ChannelAvatarPreviewParameterProvider : PreviewParameterProvider<Pair<User, Channel>> {
    override val values: Sequence<Pair<User, Channel>> = sequenceOf(
        // Channel with an image
        PreviewUserData.user1 to Channel().apply {
            image = "https://picsum.photos/id/237/128/128"
            members = listOf(
                Member(user = PreviewUserData.user1),
                Member(user = PreviewUserData.user2),
            )
        },
        // Direct chat with online user
        PreviewUserData.user1 to Channel().apply {
            members = listOf(
                Member(user = PreviewUserData.user1),
                Member(user = PreviewUserData.user2.copy(online = true)),
            )
        },
        // Group chat with 2 users
        PreviewUserData.user1 to Channel().apply {
            members = listOf(
                Member(user = PreviewUserData.user1),
                Member(user = PreviewUserData.user2),
                Member(user = PreviewUserData.user3),
            )
        },
        // Group chat with 4 users
        PreviewUserData.user1 to Channel().apply {
            members = listOf(
                Member(user = PreviewUserData.user1),
                Member(user = PreviewUserData.user2),
                Member(user = PreviewUserData.user3),
                Member(user = PreviewUserData.user4),
                Member(user = PreviewUserData.user5),
            )
        }
    )
}
