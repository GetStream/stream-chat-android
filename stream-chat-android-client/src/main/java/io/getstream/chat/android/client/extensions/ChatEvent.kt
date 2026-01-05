/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.extensions.internal

import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.client.extensions.enrichWithCid

/**
 * Enriches the wrapped Message from the [ChatEvent] with the Channel ID.
 */
public fun ChatEvent.enrichIfNeeded(): ChatEvent = when (this) {
    is NewMessageEvent -> copy(message = message.enrichWithCid(cid))
    is MessageDeletedEvent -> copy(message = message.enrichWithCid(cid))
    is MessageUpdatedEvent -> copy(message = message.enrichWithCid(cid))
    is ReactionNewEvent -> copy(message = message.enrichWithCid(cid))
    is ReactionUpdateEvent -> copy(message = message.enrichWithCid(cid))
    is ReactionDeletedEvent -> copy(message = message.enrichWithCid(cid))
    is ChannelUpdatedEvent -> copy(message = message?.enrichWithCid(cid))
    is ChannelTruncatedEvent -> copy(message = message?.enrichWithCid(cid))
    is ChannelUpdatedByUserEvent -> copy(message = message?.enrichWithCid(cid))
    is NotificationMessageNewEvent -> copy(message = message.enrichWithCid(cid))
    else -> this
}
