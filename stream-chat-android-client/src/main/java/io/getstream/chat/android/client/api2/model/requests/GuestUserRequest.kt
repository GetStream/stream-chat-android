package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class GuestUserRequest(
    val user: GuestUser,
) {

    @JsonClass(generateAdapter = true)
    data class GuestUser(
        val id: String,
        val name: String,
    )

    companion object {
        fun create(id: String, name: String): GuestUserRequest {
            return GuestUserRequest(GuestUser(id = id, name = name))
        }
    }
}
