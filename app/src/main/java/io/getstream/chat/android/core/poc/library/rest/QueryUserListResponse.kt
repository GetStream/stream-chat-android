package io.getstream.chat.android.core.poc.library.rest

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.core.poc.library.User


class QueryUserListResponse {

    @SerializedName("users")
    @Expose
    val users = listOf<User>()
}
