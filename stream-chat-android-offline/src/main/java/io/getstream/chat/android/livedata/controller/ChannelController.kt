package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelData

/**
 * The Channel Controller exposes convenient livedata objects to build your chat interface
 * It automatically handles the incoming events and keeps users, messages, reactions, channel information up to date automatically
 * Offline storage is also handled using Room
 *
 * The most commonly used livedata objects are
 *
 * - .messages (the livedata for the list of messages)
 * - .channelData (livedata object with the channel name, image, etc.)
 * - .members (livedata object with the members of this channel)
 * - .watchers (the people currently watching this channel)
 * - .typing (who is currently typing)
 *
 */
public sealed interface ChannelController
