package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import java.util.*


data class Watcher(val id: String) : UserEntity {

    var user: User? = null
    @SerializedName("created_at")
    var createdAt: Date? = null

    override fun getUserId(): String {
        return user!!.id
    }
}