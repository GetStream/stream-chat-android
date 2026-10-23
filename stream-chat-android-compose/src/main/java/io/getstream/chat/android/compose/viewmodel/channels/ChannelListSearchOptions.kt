/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.viewmodel.channels

import io.getstream.chat.android.models.AndFilterObject
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter

/**
 * Builds the filters and sort that [ChannelListViewModel] uses to search channels and messages.
 *
 * Extend this class and override the functions you want to change. Each default implementation returns the
 * filter or sort the view model uses when no options are provided.
 *
 * The functions are called on the main thread when a search request is built, so they must return quickly and must
 * not block. They are called again for the next page of a message search, and for the next page of a channel search
 * in a channel list created with a filter. In a channel list that uses a predefined filter or a channel group, the
 * next page of a channel search reuses the filter and sort of the first page.
 */
public open class ChannelListSearchOptions {

    /**
     * Returns the filter used to query channels while a channel search is active.
     *
     * By default, it matches channels whose `name` matches the query with an `autocomplete` filter, and that the
     * current user is a member of.
     * When the channel list has its own filter ([ChannelSearchParams.listFilter]), the default also applies it.
     *
     * @param params The current channel search.
     * @return The full filter sent with the channel query.
     */
    @Suppress("SpreadOperator")
    public open fun channelFilter(params: ChannelSearchParams): FilterObject {
        val searchFilter = Filters.and(
            Filters.autocomplete("name", params.query),
            Filters.`in`("members", params.currentUser?.id.orEmpty()),
        )
        return when (val listFilter = params.listFilter) {
            null -> searchFilter
            is AndFilterObject -> Filters.and(*(listFilter.filterObjects + searchFilter).toTypedArray())
            else -> Filters.and(listFilter, searchFilter)
        }
    }

    /**
     * Returns the sort used to query channels while a channel search is active.
     *
     * By default, it is the sort of the channel list ([ChannelSearchParams.listSort]), or the server-side default
     * when the channel list has no sort of its own.
     *
     * @param params The current channel search.
     * @return The sort sent with the channel query.
     */
    public open fun channelSort(params: ChannelSearchParams): QuerySorter<Channel> =
        params.listSort ?: QuerySortByField()

    /**
     * Returns the filter that selects the channels in which messages are searched.
     *
     * By default, it is the filter of the channel list ([MessageSearchParams.listFilter]). When the channel list has
     * no filter of its own, the default matches the channels the current user is a member of.
     *
     * @param params The current message search.
     * @return The channel filter sent with the message search, or `null` to skip the search.
     */
    public open fun messageChannelFilter(params: MessageSearchParams): FilterObject? =
        params.listFilter ?: params.currentUser?.id?.let { userId -> Filters.`in`("members", listOf(userId)) }

    /**
     * Returns the filter that messages must match to be part of the message search results.
     *
     * By default, it matches messages whose `text` matches the query with an `autocomplete` filter.
     *
     * @param params The current message search.
     * @return The message filter sent with the message search.
     */
    public open fun messageFilter(params: MessageSearchParams): FilterObject =
        Filters.autocomplete("text", params.query)

    /**
     * The state of a channel search.
     *
     * @param query The text the user searches for.
     * @param currentUser The connected user, or `null` when no user is connected.
     * @param listFilter The filter of the channel list. It is `null` for a channel list that uses a predefined
     * filter or a channel group, because the server owns their filter.
     * @param listSort The sort of the channel list. It is `null` for a channel list that uses a predefined filter
     * or a channel group, because the server owns their sort.
     */
    public data class ChannelSearchParams(
        val query: String,
        val currentUser: User?,
        val listFilter: FilterObject?,
        val listSort: QuerySorter<Channel>?,
    )

    /**
     * The state of a message search.
     *
     * @param query The text the user searches for.
     * @param currentUser The connected user, or `null` when no user is connected.
     * @param listFilter The filter of the channel list, or the default filter when the app did not set one. It is
     * `null` for a channel list that uses a predefined filter or a channel group, because the server owns their
     * filter.
     */
    public data class MessageSearchParams(
        val query: String,
        val currentUser: User?,
        val listFilter: FilterObject?,
    )
}
