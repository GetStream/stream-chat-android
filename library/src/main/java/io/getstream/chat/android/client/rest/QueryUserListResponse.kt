package io.getstream.chat.android.client.rest

import io.getstream.chat.android.client.User


data class QueryUserListResponse(val users: List<User> = emptyList())
