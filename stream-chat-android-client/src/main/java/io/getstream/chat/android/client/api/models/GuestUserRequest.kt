package io.getstream.chat.android.client.api.models

internal data class GuestUserRequest constructor(
    val id: String,
    val name: String
) {

    var user = GuestUserBody(id, name)

    data class GuestUserBody(
        val id: String,
        val name: String
    )
}
