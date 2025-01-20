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

package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelUserRead
import io.getstream.chat.android.models.ChannelTransformer
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.UserId
import java.util.Date

/**
 * Transform [DownstreamChannelUserRead] to [ChannelUserRead].
 *
 * @param currentUserId the current user id.
 * @param lastReceivedEventDate the last received event date.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the channel's messages.
 */
internal fun DownstreamChannelUserRead.toDomain(
    currentUserId: UserId?,
    lastReceivedEventDate: Date,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): ChannelUserRead =
    ChannelUserRead(
        user = user.toDomain(currentUserId, channelTransformer, messageTransformer),
        lastReceivedEventDate = lastReceivedEventDate,
        lastRead = last_read,
        unreadMessages = unread_messages,
        lastReadMessageId = last_read_message_id,
    )
