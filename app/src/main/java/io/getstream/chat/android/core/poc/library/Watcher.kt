package io.getstream.chat.android.core.poc.library

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName


class Watcher : UserEntity {

    @SerializedName("user")
    @Expose
    lateinit var user: User
    val createdAt: Long = 0

    override fun equals(obj: Any?): Boolean {
        if(obj == null) return false
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