package io.getstream.chat.android.client.api.models


class ChannelWatchRequest : ChannelQueryRequest() {
    override fun cloneOpts(): ChannelWatchRequest {
        val clone = ChannelWatchRequest()
        clone.state = state
        clone.watch = watch
        clone.presence = presence
        clone.messages = HashMap(messages)
        clone.data = HashMap(data)
        return clone
    }

    override fun withData(data: Map<String, Any>): ChannelWatchRequest {
        val clone = cloneOpts()
        clone.data.putAll(data)
        return clone
    }

    override fun withMessages(limit: Int): ChannelWatchRequest {
        val clone = cloneOpts()
        val messages: MutableMap<String, Any> = HashMap()
        messages["limit"] = limit
        clone.messages = messages
        return clone
    }

    override fun withMessages(
        direction: Pagination,
        messageId: String,
        limit: Int
    ): ChannelWatchRequest {
        val clone = cloneOpts()
        val messages: MutableMap<String, Any> = HashMap()
        messages["limit"] = limit
        messages[direction.toString()] = messageId
        clone.messages = messages
        return clone
    }

    override fun withPresence(): ChannelWatchRequest {
        val clone = cloneOpts()
        clone.presence = true
        return clone
    }

    init {
        watch = true
        presence = false
        state = true
    }
}
