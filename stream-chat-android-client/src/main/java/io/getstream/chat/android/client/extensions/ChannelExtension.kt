package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User

public fun Channel.isAnonymousChannel(): Boolean = id.isAnonymousChannelId()

/**
 * Checks if [Channel] is muted for [user].
 *
 * @return True if the channel is muted for [user].
 */
public fun Channel.isMutedFor(user: User): Boolean = user.channelMutes.any { mute -> mute.channel.cid == cid }
