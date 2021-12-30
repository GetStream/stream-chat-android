package io.getstream.chat.android.compose.previewdata

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import java.util.Date

/**
 * Provides sample channels that will be used to render previews.
 */
internal object PreviewChannelData {

    val channelWithImage: Channel = Channel().apply {
        cid = "channelType:channelId1"
        image = "https://picsum.photos/id/237/128/128"
        members = listOf(
            Member(user = PreviewUserData.user1),
            Member(user = PreviewUserData.user2),
        )
    }

    val channelWithOnlineUser: Channel = Channel().apply {
        cid = "channelType:channelId2"
        members = listOf(
            Member(user = PreviewUserData.user1),
            Member(user = PreviewUserData.user2.copy(online = true)),
        )
    }

    val channelWithFewMembers: Channel = Channel().apply {
        cid = "channelType:channelId3"
        members = listOf(
            Member(user = PreviewUserData.user1),
            Member(user = PreviewUserData.user2),
            Member(user = PreviewUserData.user3),
        )
    }

    val channelWithManyMembers: Channel = Channel().apply {
        cid = "channelType:channelId4"
        members = listOf(
            Member(user = PreviewUserData.user1),
            Member(user = PreviewUserData.user2),
            Member(user = PreviewUserData.user3),
            Member(user = PreviewUserData.user4),
            Member(user = PreviewUserData.userWithoutImage),
        )
    }

    val channelWithMessages: Channel = Channel().apply {
        cid = "channelType:channelId5"
        members = listOf(
            Member(user = PreviewUserData.user1),
            Member(user = PreviewUserData.user2),
        )
        messages = listOf(
            PreviewMessageData.message1,
            PreviewMessageData.message2
        )
        unreadCount = 2
        lastMessageAt = Date()
    }
}
