package io.getstream.chat.ui.sample.util.extensions

import io.getstream.chat.android.offline.model.channel.ChannelData

public fun ChannelData.isAnonymousChannel(): Boolean = channelId.startsWith("!members")
