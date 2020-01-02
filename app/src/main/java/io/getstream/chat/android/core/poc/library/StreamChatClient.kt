package io.getstream.chat.android.core.poc.library

import android.text.TextUtils
import io.getstream.chat.android.core.poc.library.requests.ChannelsQuery


class StreamChatClient {

    private val api = Api(RetrofitApiBuilder().build())

    fun setUser(user: ChatUser) {

    }

    fun getState(): ClientState{
        return ClientState()
    }

    fun fromCurrentUser(entity: UserEntity): Boolean {
        val otherUserId = entity.getUserId() ?: return false
        return if (getUser() == null) false else TextUtils.equals(getUserId(), otherUserId)
    }

    fun getUserId():String{
        return ""
    }

    fun getUser():User{
        return User("")
    }

    fun disconnect() {

    }

    fun queryChannel() {

    }

    fun queryChannels(query: ChannelsQuery = ChannelsQuery()): Call<List<ChatChannel>> {
        return api.queryChannels(query)
    }
}