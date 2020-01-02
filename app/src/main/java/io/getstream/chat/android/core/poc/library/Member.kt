package io.getstream.chat.android.core.poc.library

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName


class Member : UserEntity {
    @SerializedName("user")
    @Expose
    lateinit var user: User
    @SerializedName("role")
    @Expose
    var role: String = ""
    @SerializedName("created_at")
    @Expose
    var createdAt: Long = 0
    @SerializedName("updated_at")
    @Expose
    var updatedAt: Long = 0
    @SerializedName("invited")
    @Expose
    var isInvited = false
    @SerializedName("invite_accepted_at")
    @Expose
    var inviteAcceptedAt: Long = 0
    @SerializedName("invite_rejected_at")
    @Expose
    var inviteRejectedAt: Long = 0

    override fun getUserId(): String {
        return user.id
    }
}
