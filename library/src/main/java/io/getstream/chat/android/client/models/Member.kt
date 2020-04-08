package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import java.util.*


data class Member(
    override var user: User,
    var role: String = "",
    @SerializedName("created_at")
    var createdAt: Date? = null,
    @SerializedName("updated_at")
    var updatedAt: Date? = null,
    @SerializedName("invited")
    var isInvited: Boolean = false,
    @SerializedName("invite_accepted_at")
    var inviteAcceptedAt: Date? = null,
    @SerializedName("invite_rejected_at")
    var inviteRejectedAt: Date? = null
) : UserEntity
