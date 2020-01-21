package io.getstream.chat.android.core.poc.library.rest

import com.google.gson.annotations.SerializedName


class RemoveMembersRequest(
    @SerializedName("remove_members")
    val members: List<String>
)
