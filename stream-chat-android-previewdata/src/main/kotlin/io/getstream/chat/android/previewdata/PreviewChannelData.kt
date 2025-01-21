/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.previewdata

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Member
import java.util.Date

/**
 * Provides sample channels that will be used to render previews.
 */
public object PreviewChannelData {

    public val channelWithImage: Channel = Channel(
        type = "channelType",
        id = "channelId1",
        image = "https://picsum.photos/id/237/128/128",
        members = listOf(
            Member(user = PreviewUserData.user1),
            Member(user = PreviewUserData.user2),
        ),
    )

    public val channelWithOneUser: Channel = Channel(
        type = "channelType",
        id = "channelId2",
        members = listOf(
            Member(user = PreviewUserData.user1),
        ),
    )

    public val channelWithOnlineUser: Channel = Channel(
        type = "channelType",
        id = "channelId3",
        members = listOf(
            Member(user = PreviewUserData.user1),
            Member(user = PreviewUserData.user2.copy(online = true)),
        ),
    )

    public val channelWithFewMembers: Channel = Channel(
        type = "channelType",
        id = "channelId4",
        members = listOf(
            Member(user = PreviewUserData.user1),
            Member(user = PreviewUserData.user2),
            Member(user = PreviewUserData.user3),
        ),
    )

    public val channelWithManyMembers: Channel = Channel(
        type = "channelType",
        id = "channelId5",
        members = listOf(
            Member(user = PreviewUserData.user1),
            Member(user = PreviewUserData.user2),
            Member(user = PreviewUserData.user3),
            Member(user = PreviewUserData.user4),
            Member(user = PreviewUserData.userWithoutImage),
        ),
    )

    public val channelWithMessages: Channel = Channel(
        type = "channelType",
        id = "channelId6",
        members = listOf(
            Member(user = PreviewUserData.user1),
            Member(user = PreviewUserData.user2),
        ),
        messages = listOf(
            PreviewMessageData.message1,
            PreviewMessageData.message2,
        ),
        channelLastMessageAt = Date(),
    )
}
