package io.getstream.chat.android.livedata.model

import io.getstream.chat.android.client.models.Config

internal typealias ChannelConfig = Pair<String, Config>

internal val ChannelConfig.type: String
    get() = this.first

internal val ChannelConfig.config: Config
    get() = this.second
