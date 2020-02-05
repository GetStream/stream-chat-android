package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import java.util.*


class Watcher : UserEntity {

    @SerializedName("user")

    lateinit var user: User
    val createdAt = Date()

    override fun equals(obj: Any?): Boolean {
        if (obj == null) return false
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as Watcher?
        return other!!.user.equals(user)
    }

    override fun getUserId(): String {
        return user.id
    }
}