package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import java.util.*


data class Member(
    override var user: User,
    @SerializedName("created_at")
    var createdAt: Date? = null,
    @SerializedName("updated_at")
    var updatedAt: Date? = null
) : UserEntity
