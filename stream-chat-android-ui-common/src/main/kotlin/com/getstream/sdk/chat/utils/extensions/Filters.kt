package com.getstream.sdk.chat.utils.extensions

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public fun Filters.defaultChannelListFilter(user: User?): FilterObject {
    val userFiler = if (user == null) neutral() else `in`("members", listOf(user.id))
    return and(
        eq("type", "messaging"),
        userFiler,
        or(notExists("draft"), ne("draft", true)),
    )
}
