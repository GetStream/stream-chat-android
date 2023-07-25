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