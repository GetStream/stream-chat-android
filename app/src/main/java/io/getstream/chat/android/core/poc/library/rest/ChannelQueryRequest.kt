package io.getstream.chat.android.core.poc.library.rest

import io.getstream.chat.android.core.poc.library.BaseQueryChannelRequest


class ChannelQueryRequest : BaseQueryChannelRequest<ChannelQueryRequest>() {

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

    fun withData(data: Map<String, Any>): ChannelQueryRequest {
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

    fun withMessages(limit: Int): ChannelQueryRequest {
        val clone = cloneOpts()
        val messages: MutableMap<String, Any> = HashMap()
        messages["limit"] = limit
        clone.messages = messages
        return clone
    }

    fun withMessages(
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

    init {
        watch = false
        presence = false
        state = true
    }
}
