package io.getstream.chat.android.client.api.models


open class QueryChannelRequest : ChannelRequest<QueryChannelRequest> {

    override var state: Boolean = false
    override var watch: Boolean = false
    override var presence: Boolean = false

    val messages = mutableMapOf<String, Any>()
    val watchers = mutableMapOf<String, Any>()
    val members = mutableMapOf<String, Any>()
    val data = mutableMapOf<String, Any>()

    open fun withData(data: Map<String, Any>): QueryChannelRequest {
        this.data.putAll(data)
        return this
    }

    open fun withMembers(limit: Int, offset: Int): QueryChannelRequest {
        val members: MutableMap<String, Any> = HashMap()
        members["limit"] = limit
        members["offset"] = offset
        this.members.putAll(members)
        return this
    }

    open fun withWatchers(limit: Int, offset: Int): QueryChannelRequest {
        val watchers: MutableMap<String, Any> = HashMap()
        watchers["limit"] = limit
        watchers["offset"] = offset
        this.watchers.putAll(watchers)
        return this
    }

    open fun withMessages(limit: Int): QueryChannelRequest {
        val messages: MutableMap<String, Any> = HashMap()
        messages["limit"] = limit
        this.messages.putAll(messages)
        return this
    }



    open fun withMessages(direction: Pagination, messageId: String, limit: Int): QueryChannelRequest {
        val messages: MutableMap<String, Any> = HashMap()
        messages["limit"] = limit
        messages[direction.toString()] = messageId
        this.messages.putAll(messages)
        return this
    }

    fun isFilteringNewerMessages(): Boolean {
        if (messages.isEmpty()) {
            return false
        }
        val keys = messages.keys
        return keys.contains(Pagination.GREATER_THAN.toString()) || keys.contains(Pagination.GREATER_THAN_OR_EQUAL.toString())
    }

    fun filteringOlderMessages(): Boolean {
        if (messages.isEmpty()) {
            return false
        }
        val keys = messages.keys
        return keys.contains(Pagination.LESS_THAN.toString()) || keys.contains(Pagination.LESS_THAN_OR_EQUAL.toString())
    }

}
