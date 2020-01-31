package io.getstream.chat.android.client.rest

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class GuestUserRequest constructor(id: String, name: String?) {

    @SerializedName("user")
    @Expose
    var user: GuestUserBody

    init {
        this.user = GuestUserBody(id = id, name = name)
    }

    data class GuestUserBody(
        @SerializedName("id")
        @Expose
        val id: String,

        @SerializedName("name")
        @Expose
        val name: String?
    )
}
