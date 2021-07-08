package io.getstream.chat.android.client.api.models

internal data class PartialUpdateUsersRequest(val users: List<PartialUpdateUser>)

internal data class PartialUpdateUser(
    val id: String,
    val set: Map<String, Any>,
    val unset: List<String>,
)
