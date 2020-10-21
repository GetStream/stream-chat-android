package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.User

internal data class UpdateUsersResponse(val users: Map<String, User>)
