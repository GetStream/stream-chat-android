package com.getstream.sdk.chat.utils.extensions

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public fun Filters.defaultChannelListFilter(user: User?): FilterObject? {
    return if (user == null) {
        null
    } else {
        and(
            eq("type", "messaging"),
            `in`("members", listOf(user.id)),
            or(notExists("draft"), ne("draft", true)),
        )
    }
}
