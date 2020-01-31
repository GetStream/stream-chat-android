package io.getstream.chat.android.client.rest

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.User


class QueryUserListResponse {

    @SerializedName("users")
    @Expose
    val users = listOf<User>()
}
