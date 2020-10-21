package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.User

public data class UpdateUsersRequest(
    val users: Map<String, User>
)
