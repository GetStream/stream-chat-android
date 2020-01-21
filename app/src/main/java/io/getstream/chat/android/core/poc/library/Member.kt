package io.getstream.chat.android.core.poc.library

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.core.poc.library.utils.UndefinedDate
import java.util.*


class Member : UserEntity {
    @SerializedName("user")
    
    lateinit var user: User
    @SerializedName("role")
    
    var role: String = ""
    @SerializedName("created_at")
    
    var createdAt:Date = UndefinedDate
    @SerializedName("updated_at")
    
    var updatedAt:Date = UndefinedDate
    @SerializedName("invited")
    
    var isInvited = false
    @SerializedName("invite_accepted_at")
    
    var inviteAcceptedAt:Date = UndefinedDate
    @SerializedName("invite_rejected_at")
    
    var inviteRejectedAt:Date = UndefinedDate

    override fun getUserId(): String {
        return user.id
    }
}
