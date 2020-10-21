package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.User

internal data class QueryUserListResponse(val users: List<User> = emptyList())
