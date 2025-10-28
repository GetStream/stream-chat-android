/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.core.internal.InternalStreamChatApi

@Suppress("TooManyFunctions")
public open class QueryChannelRequest : ChannelRequest<QueryChannelRequest> {

    override var state: Boolean = false
    override var watch: Boolean = false
    override var presence: Boolean = false

    @InternalStreamChatApi
    public var shouldRefresh: Boolean = false

    @InternalStreamChatApi
    public var isWatchChannel: Boolean = false

    @InternalStreamChatApi
    public var isNotificationUpdate: Boolean = false

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
        state = true
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
        return keys.contains(Pagination.GREATER_THAN.toString()) ||
            keys.contains(Pagination.GREATER_THAN_OR_EQUAL.toString())
    }

    public fun filteringOlderMessages(): Boolean {
        if (messages.isEmpty()) {
            return false
        }
        val keys = messages.keys
        return keys.contains(Pagination.LESS_THAN.toString()) ||
            keys.contains(Pagination.LESS_THAN_OR_EQUAL.toString())
    }

    public fun isFilteringAroundIdMessages(): Boolean {
        if (messages.isEmpty()) {
            return false
        }
        val keys = messages.keys
        return keys.contains(Pagination.AROUND_ID.toString())
    }

    /**
     * @return Whether the request contains any of [Pagination] values or not. If it does the messages are being
     * filtered.
     */
    public fun isFilteringMessages(): Boolean = Pagination.values().map { it.toString() }.intersect(messages.keys).isNotEmpty()

    /**
     * Returns offset of watchers for a requested channel.
     */
    public fun watchersOffset(): Int = watchers[KEY_OFFSET] as? Int ?: 0

    /**
     * Returns offset of members for a requested channel.
     */
    public fun membersOffset(): Int = watchers[KEY_OFFSET] as? Int ?: 0

    /**
     * Returns limit of messages for a requested channel.
     */
    public fun messagesLimit(): Int = messages[KEY_LIMIT] as? Int ?: 0

    /**
     * Returns limit of watchers for a requested channel.
     */
    public fun watchersLimit(): Int = watchers[KEY_LIMIT] as? Int ?: 0

    /**
     * Returns limit of members for a requested channel.
     */
    public fun membersLimit(): Int = messages[KEY_LIMIT] as? Int ?: 0

    /**
     * Returns a pair value of [Pagination] and id of message for this pagination. Can be absent.
     */
    public fun pagination(): Pair<Pagination, String>? {
        if (messages.isEmpty()) {
            return null
        }
        val keys = messages.keys
        val pagination = Pagination.values().firstOrNull { keys.contains(it.toString()) }
        return if (pagination != null) {
            pagination to (messages[pagination.toString()] as? String ?: "")
        } else {
            null
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QueryChannelRequest) return false
        if (state != other.state) return false
        if (watch != other.watch) return false
        if (presence != other.presence) return false
        if (shouldRefresh != other.shouldRefresh) return false
        if (messages != other.messages) return false
        if (watchers != other.watchers) return false
        if (members != other.members) return false
        if (data != other.data) return false
        if (isNotificationUpdate != other.isNotificationUpdate) return false
        return true
    }

    override fun hashCode(): Int {
        var result = state.hashCode()
        result = 31 * result + watch.hashCode()
        result = 31 * result + presence.hashCode()
        result = 31 * result + shouldRefresh.hashCode()
        result = 31 * result + messages.hashCode()
        result = 31 * result + watchers.hashCode()
        result = 31 * result + members.hashCode()
        result = 31 * result + data.hashCode()
        result = 31 * result + isNotificationUpdate.hashCode()
        return result
    }

    override fun toString(): String = "QueryChannelRequest(" +
        "state=$state, " +
        "watch=$watch, " +
        "presence=$presence, " +
        "shouldRefresh=$shouldRefresh, " +
        "isWatchChannel=$isWatchChannel, " +
        "isNotificationUpdate=$isNotificationUpdate, " +
        "messages=$messages, " +
        "watchers=$watchers, " +
        "members=$members, " +
        "data=$data)"

    internal companion object {
        private const val KEY_LIMIT = "limit"
        private const val KEY_OFFSET = "offset"
        internal const val KEY_MEMBERS = "members"
    }
}
