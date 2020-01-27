package io.getstream.chat.android.core.poc.library.rest

import com.google.gson.annotations.SerializedName


class GuestUserRequest constructor(id: String, name: String?) {

    @SerializedName("user")
    val user: GuestUserBody

    init {
        user = GuestUserBody(id, name)
    }

    data class GuestUserBody(
        @SerializedName("id")
        val id: String,

        @SerializedName("name")
        val name: String?
    )
}
