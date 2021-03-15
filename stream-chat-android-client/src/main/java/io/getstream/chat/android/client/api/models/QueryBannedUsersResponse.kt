package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.BannedUser

internal data class QueryBannedUsersResponse(
    val bans: List<BannedUser>,
)
