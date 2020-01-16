package io.getstream.chat.android.core.poc.library

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.core.poc.library.utils.UndefinedDate
import java.util.*


class Member : UserEntity {
    @SerializedName("user")
    @Expose
    lateinit var user: User
    @SerializedName("role")
    @Expose
    var role: String = ""
    @SerializedName("created_at")
    @Expose
    var createdAt:Date = UndefinedDate
    @SerializedName("updated_at")
    @Expose
    var updatedAt:Date = UndefinedDate
    @SerializedName("invited")
    @Expose
    var isInvited = false
    @SerializedName("invite_accepted_at")
    @Expose
    var inviteAcceptedAt:Date = UndefinedDate
    @SerializedName("invite_rejected_at")
    @Expose
    var inviteRejectedAt:Date = UndefinedDate

    override fun getUserId(): String {
        return user.id
    }
}
