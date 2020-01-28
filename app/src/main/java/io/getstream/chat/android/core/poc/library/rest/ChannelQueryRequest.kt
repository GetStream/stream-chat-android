package io.getstream.chat.android.core.poc.library.rest

import io.getstream.chat.android.core.poc.library.BaseQueryChannelRequest


open class ChannelQueryRequest : BaseQueryChannelRequest<ChannelQueryRequest>() {

    var messages = mutableMapOf<String, Any>()
    var watchers = mutableMapOf<String, Any>()
    var members = mutableMapOf<String, Any>()
    var data = mutableMapOf<String, Any>()

    override fun cloneOpts(): ChannelQueryRequest {
        val _this = ChannelQueryRequest()
        _this.state = state
        _this.watch = watch
        _this.presence = presence
        _this.messages = HashMap(messages)
        _this.watchers = HashMap(watchers)
        _this.members = HashMap(members)
        _this.data = HashMap(data)
        return _this
    }

    open fun withData(data: Map<String, Any>): ChannelQueryRequest {
        val clone = cloneOpts()
        clone.data = data.toMutableMap()
        return clone
    }

    override fun withPresence(): ChannelQueryRequest {
        val clone = cloneOpts()
        clone.presence = true
        return clone
    }

    fun withMembers(limit: Int, offset: Int): ChannelQueryRequest {
        val clone = cloneOpts()
        val members: MutableMap<String, Any> = HashMap()
        members["limit"] = limit
        members["offset"] = offset
        clone.members = members
        return clone
    }

    fun withWatchers(limit: Int, offset: Int): ChannelQueryRequest {
        val clone = cloneOpts()
        val watchers: MutableMap<String, Any> = HashMap()
        watchers["limit"] = limit
        watchers["offset"] = offset
        clone.watchers = watchers
        return clone
    }

    open fun withMessages(limit: Int): ChannelQueryRequest {
        val clone = cloneOpts()
        val messages: MutableMap<String, Any> = HashMap()
        messages["limit"] = limit
        clone.messages = messages
        return clone
    }

    open fun withMessages(
        direction: Pagination,
        messageId: String,
        limit: Int
    ): ChannelQueryRequest {
        val clone = cloneOpts()
        val messages: MutableMap<String, Any> = HashMap()
        messages["limit"] = limit
        messages[direction.toString()] = messageId
        clone.messages = messages
        return clone
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChannelQueryRequest

        if (messages != other.messages) return false
        if (watchers != other.watchers) return false
        if (members != other.members) return false
        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        var result = messages.hashCode()
        result = 31 * result + watchers.hashCode()
        result = 31 * result + members.hashCode()
        result = 31 * result + data.hashCode()
        return result
    }

    init {
        watch = false
        presence = false
        state = true
    }


}
