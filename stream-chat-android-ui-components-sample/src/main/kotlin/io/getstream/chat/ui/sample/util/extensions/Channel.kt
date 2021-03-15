package io.getstream.chat.ui.sample.util.extensions

import io.getstream.chat.android.livedata.ChannelData

public fun ChannelData.isAnonymousChannel(): Boolean = channelId.startsWith("!members")
