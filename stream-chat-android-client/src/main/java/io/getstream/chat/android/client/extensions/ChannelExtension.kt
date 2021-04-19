package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.EXTRA_DATA_MUTED

public fun Channel.isAnonymousChannel(): Boolean = id.isAnonymousChannelId()

public var Channel.isMuted : Boolean
    get() = extraData[EXTRA_DATA_MUTED] as Boolean? ?: false
    set(value) {
        extraData[EXTRA_DATA_MUTED] = value
    }
