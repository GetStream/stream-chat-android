/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.state.event.handler.internal.utils

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMarkUnreadEvent
import io.getstream.chat.android.models.ChannelUserRead

internal val ChatEvent.realType get() = when (this) {
    is ConnectedEvent -> "connection.connected"
    else -> type
}

internal fun MessageReadEvent.toChannelUserRead() = ChannelUserRead(
    user = user,
    lastReceivedEventDate = createdAt,
    lastRead = createdAt,
// TODO: remove this once the backend is fixed and is sending us the number of unread messages
    unreadMessages = 0,
)
internal fun NotificationMarkReadEvent.toChannelUserRead() = ChannelUserRead(
    user = user,
    lastReceivedEventDate = createdAt,
    lastRead = createdAt,
// TODO: remove this once the backend is fixed and is sending us the number of unread messages
    unreadMessages = 0,
)

internal fun NotificationMarkUnreadEvent.toChannelUserRead() = ChannelUserRead(
    user = user,
    lastReceivedEventDate = createdAt,
    lastRead = lastReadMessageAt,
    unreadMessages = unreadMessages,
)

internal fun MarkAllReadEvent.toChannelUserRead() = ChannelUserRead(
    user = user,
    lastReceivedEventDate = createdAt,
    lastRead = createdAt,
    unreadMessages = 0,
)
