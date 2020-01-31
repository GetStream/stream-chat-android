package io.getstream.chat.android.client.rest

import com.google.gson.annotations.SerializedName


class RemoveMembersRequest(
    @SerializedName("remove_members")
    val members: List<String>
)
