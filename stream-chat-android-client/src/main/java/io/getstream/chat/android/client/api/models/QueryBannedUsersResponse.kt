package io.getstream.chat.android.client.api.models

internal data class QueryBannedUsersResponse(
    val bans: List<BannedUserResponse>,
)
