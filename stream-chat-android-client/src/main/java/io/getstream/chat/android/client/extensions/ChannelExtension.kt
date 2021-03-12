package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.client.models.Channel

public fun Channel.isAnonymousChannel(): Boolean = cid.isAnonymousChannelId()
