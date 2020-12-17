package com.getstream.sdk.chat.utils.extensions

import io.getstream.chat.android.client.models.Channel

internal val Channel.isDraft: Boolean
    get() = getExtraValue("draft", false)
