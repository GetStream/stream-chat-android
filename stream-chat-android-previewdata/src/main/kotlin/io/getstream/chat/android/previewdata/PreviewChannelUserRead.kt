/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.models.ChannelUserRead
import java.util.Date

public object PreviewChannelUserRead {

    public val channelUserRead1: ChannelUserRead = ChannelUserRead(
        user = PreviewUserData.user1,
        lastReceivedEventDate = Date(),
        unreadMessages = 1,
        lastRead = Date(),
        lastReadMessageId = null,
    )

    public val channelUserRead2: ChannelUserRead = ChannelUserRead(
        user = PreviewUserData.user2,
        lastReceivedEventDate = Date(),
        unreadMessages = 2,
        lastRead = Date(),
        lastReadMessageId = null,
    )
}
