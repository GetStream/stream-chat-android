package io.getstream.chat.android.client.models

import java.util.*


class Watcher : UserEntity {

    lateinit var user: User
    lateinit var createdAt:Date

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