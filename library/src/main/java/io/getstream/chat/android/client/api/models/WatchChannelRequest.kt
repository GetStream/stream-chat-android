package io.getstream.chat.android.client.api.models


class WatchChannelRequest : QueryChannelRequest() {

    init {
        watch = true
        presence = false
        state = true
    }

    override fun withData(data: Map<String, Any>): WatchChannelRequest {
        return super.withData(data) as WatchChannelRequest
    }

    override fun withMembers(limit: Int, offset: Int): WatchChannelRequest {
        return super.withMembers(limit, offset) as WatchChannelRequest
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

    override fun withWatchers(limit: Int, offset: Int): WatchChannelRequest {
        return super.withWatchers(limit, offset) as WatchChannelRequest
    }

    override fun withMessages(limit: Int): WatchChannelRequest {
        return super.withMessages(limit) as WatchChannelRequest
    }

    override fun withMessages(direction: Pagination, messageId: String, limit: Int): WatchChannelRequest {
        return super.withMessages(direction, messageId, limit) as WatchChannelRequest
    }
}