package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.parser.IgnoreSerialisation

internal data class GuestUserRequest constructor(
    @IgnoreSerialisation
    val id: String,
    @IgnoreSerialisation
    val name: String
) {

    var user = GuestUserBody(id, name)

    data class GuestUserBody(
        val id: String,
        val name: String
    )
}
