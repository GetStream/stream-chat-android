package io.getstream.chat.android.client.api.models

public open class QueryChannelRequest : ChannelRequest<QueryChannelRequest> {

    override var state: Boolean = false
    override var watch: Boolean = false
    override var presence: Boolean = false

    public val messages: MutableMap<String, Any> = mutableMapOf()
    public val watchers: MutableMap<String, Any> = mutableMapOf()
    public val members: MutableMap<String, Any> = mutableMapOf()
    public val data: MutableMap<String, Any> = mutableMapOf()

    public open fun withData(data: Map<String, Any>): QueryChannelRequest {
        this.data.putAll(data)
        return this
    }

    public open fun withMembers(limit: Int, offset: Int): QueryChannelRequest {
        state = true
        val members: MutableMap<String, Any> = HashMap()
        members[KEY_LIMIT] = limit
        members[KEY_OFFSET] = offset
        this.members.putAll(members)
        return this
    }

    public open fun withWatchers(limit: Int, offset: Int): QueryChannelRequest {
        val watchers: MutableMap<String, Any> = HashMap()
        watchers[KEY_LIMIT] = limit
        watchers[KEY_OFFSET] = offset
        this.watchers.putAll(watchers)
        return this
    }

    public open fun withMessages(limit: Int): QueryChannelRequest {
        state = true
        val messages: MutableMap<String, Any> = HashMap()
        messages[KEY_LIMIT] = limit
        this.messages.putAll(messages)
        return this
    }

    public open fun withMessages(direction: Pagination, messageId: String, limit: Int): QueryChannelRequest {
        state = true
        val messages: MutableMap<String, Any> = HashMap()
        messages[KEY_LIMIT] = limit
        messages[direction.toString()] = messageId
        this.messages.putAll(messages)
        return this
    }

    public fun isFilteringNewerMessages(): Boolean {
        if (messages.isEmpty()) {
            return false
        }
        val keys = messages.keys
        return keys.contains(Pagination.GREATER_THAN.toString()) || keys.contains(Pagination.GREATER_THAN_OR_EQUAL.toString())
    }

    public fun filteringOlderMessages(): Boolean {
        if (messages.isEmpty()) {
            return false
        }
        val keys = messages.keys
        return keys.contains(Pagination.LESS_THAN.toString()) || keys.contains(Pagination.LESS_THAN_OR_EQUAL.toString())
    }

    public fun messagesLimit(): Int {
        return messages[KEY_LIMIT] as? Int ?: 0
    }

    public fun pagination(): Pagination? {
        if (messages.isEmpty()) {
            return null
        }
        val keys = messages.keys
        return Pagination.values().firstOrNull { keys.contains(it.toString()) }
    }

    private companion object {
        private const val KEY_LIMIT = "limit"
        private const val KEY_OFFSET = "offset"
    }
}
