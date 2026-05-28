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

package io.getstream.chat.android.ui.viewmodel.channels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.state.event.handler.chat.factory.ChatEventHandlerFactory
import io.getstream.chat.android.state.extensions.globalStateFlow
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModel.QueryMode

/**
 * Creates a channels view model factory.
 *
 * @param limit How many channels to return.
 * @param memberLimit The number of members per channel. When `null`, the server-side default is used.
 * @param messageLimit The number of messages to fetch for each channel. When `null`, the server-side default is used.
 * @param isDraftMessagesEnabled Enables or disables draft messages.
 * @param chatEventHandlerFactory The instance of [ChatEventHandlerFactory] that will be used to create [ChatEventHandler].
 *
 * @see Filters
 * @see QuerySorter
 */
public class ChannelListViewModelFactory
@Suppress("LongParameterList")
private constructor(
    private val mode: QueryMode,
    private val limit: Int,
    private val messageLimit: Int?,
    private val memberLimit: Int?,
    private val isDraftMessagesEnabled: Boolean,
    private val chatEventHandlerFactory: ChatEventHandlerFactory,
) : ViewModelProvider.Factory {

    /**
     * Builds a factory for a [ChannelListViewModel] that queries channels by an explicit filter and sort.
     *
     * @param filter How to filter the channels. When `null`, a default filter scoped to messaging
     * channels the current user is a member of is used.
     * @param sort How to sort the channels, defaults to last_updated.
     */
    @JvmOverloads
    public constructor(
        filter: FilterObject? = null,
        sort: QuerySorter<Channel> = ChannelListViewModel.DEFAULT_SORT,
        limit: Int = ChannelListViewModel.DEFAULT_CHANNEL_LIMIT,
        messageLimit: Int? = null,
        memberLimit: Int? = null,
        isDraftMessagesEnabled: Boolean = ChatUI.draftMessagesEnabled,
        chatEventHandlerFactory: ChatEventHandlerFactory = ChatEventHandlerFactory(),
    ) : this(
        mode = QueryMode.Standard(initialFilter = filter, initialSort = sort),
        limit = limit,
        messageLimit = messageLimit,
        memberLimit = memberLimit,
        isDraftMessagesEnabled = isDraftMessagesEnabled,
        chatEventHandlerFactory = chatEventHandlerFactory,
    )

    /**
     * Builds a factory for a [ChannelListViewModel] that queries channels using a predefined filter
     * resolved by the server.
     *
     * @param predefinedFilterName The name of the predefined filter registered on the backend.
     * @param filterValues Optional values interpolated into the predefined filter template.
     * @param sortValues Optional values interpolated into the predefined sort template.
     */
    @JvmOverloads
    public constructor(
        predefinedFilterName: String,
        filterValues: Map<String, Any>? = null,
        sortValues: Map<String, Any>? = null,
        limit: Int = ChannelListViewModel.DEFAULT_CHANNEL_LIMIT,
        messageLimit: Int? = null,
        memberLimit: Int? = null,
        isDraftMessagesEnabled: Boolean = ChatUI.draftMessagesEnabled,
        chatEventHandlerFactory: ChatEventHandlerFactory = ChatEventHandlerFactory(),
    ) : this(
        mode = QueryMode.Predefined(
            name = predefinedFilterName,
            filterValues = filterValues,
            sortValues = sortValues,
        ),
        limit = limit,
        messageLimit = messageLimit,
        memberLimit = memberLimit,
        isDraftMessagesEnabled = isDraftMessagesEnabled,
        chatEventHandlerFactory = chatEventHandlerFactory,
    )

    /**
     * Returns an instance of [ChannelListViewModel].
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ChannelListViewModel::class.java) {
            "ChannelListViewModelFactory can only create instances of ChannelListViewModel"
        }
        @Suppress("UNCHECKED_CAST")
        return ChannelListViewModel(
            mode = mode,
            limit = limit,
            messageLimit = messageLimit,
            memberLimit = memberLimit,
            isDraftMessagesEnabled = isDraftMessagesEnabled,
            chatEventHandlerFactory = chatEventHandlerFactory,
            chatClient = ChatClient.instance(),
            globalState = ChatClient.instance().globalStateFlow,
        ) as T
    }

    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    public class Builder
    @SinceKotlin("99999.9")
    constructor() {

        private var filter: FilterObject? = null
        private var sort: QuerySorter<Channel> = ChannelListViewModel.DEFAULT_SORT
        private var predefinedFilterName: String? = null
        private var filterValues: Map<String, Any>? = null
        private var sortValues: Map<String, Any>? = null
        private var limit: Int = ChannelListViewModel.DEFAULT_CHANNEL_LIMIT
        private var messageLimit: Int? = null
        private var memberLimit: Int? = null
        private var chatEventHandlerFactory: ChatEventHandlerFactory = ChatEventHandlerFactory()
        private var isDraftMessagesEnabled: Boolean = ChatUI.draftMessagesEnabled

        /**
         * Sets the way to filter the channels. Mutually exclusive with [predefinedFilter].
         */
        public fun filter(filter: FilterObject): Builder = apply {
            this.filter = filter
        }

        /**
         * Sets the way to sort the channels, defaults to last_updated.
         */
        public fun sort(sort: QuerySorter<Channel>): Builder = apply {
            this.sort = sort
        }

        /**
         * Configures the factory to query channels via a predefined filter resolved by the server.
         * Mutually exclusive with [filter].
         */
        public fun predefinedFilter(name: String): Builder = apply {
            this.predefinedFilterName = name
        }

        /**
         * Sets the values interpolated into the predefined filter template. Has no effect unless
         * [predefinedFilter] was called.
         */
        public fun filterValues(values: Map<String, Any>): Builder = apply {
            this.filterValues = values
        }

        /**
         * Sets the values interpolated into the predefined sort template. Has no effect unless
         * [predefinedFilter] was called.
         */
        public fun sortValues(values: Map<String, Any>): Builder = apply {
            this.sortValues = values
        }

        /**
         * Sets the number of channels to return.
         */
        public fun limit(limit: Int): Builder = apply {
            this.limit = limit
        }

        /**
         * Sets the number of messages to fetch for each channel.
         * When `null`, the server-side default is used.
         */
        public fun messageLimit(messageLimit: Int?): Builder = apply {
            this.messageLimit = messageLimit
        }

        /**
         * Sets the number of members per channel.
         * When `null`, the server-side default is used.
         */
        public fun memberLimit(memberLimit: Int?): Builder = apply {
            this.memberLimit = memberLimit
        }

        /**
         * The instance of [ChatEventHandlerFactory] that will be used to create [ChatEventHandler].
         */
        public fun chatEventHandlerFactory(chatEventHandlerFactory: ChatEventHandlerFactory): Builder = apply {
            this.chatEventHandlerFactory = chatEventHandlerFactory
        }

        /**
         * Enables or disables draft messages.
         */
        public fun isDraftMessagesEnabled(isDraftMessagesEnabled: Boolean): Builder = apply {
            this.isDraftMessagesEnabled = isDraftMessagesEnabled
        }

        /**
         * Builds [ChannelListViewModelFactory] instance.
         *
         * @throws IllegalStateException if both [filter] and [predefinedFilter] were set.
         */
        public fun build(): ViewModelProvider.Factory {
            val name = predefinedFilterName
            return if (name != null) {
                check(filter == null) {
                    "ChannelListViewModelFactory.Builder: filter() and predefinedFilter() are mutually exclusive."
                }
                ChannelListViewModelFactory(
                    predefinedFilterName = name,
                    filterValues = filterValues,
                    sortValues = sortValues,
                    limit = limit,
                    messageLimit = messageLimit,
                    memberLimit = memberLimit,
                    chatEventHandlerFactory = chatEventHandlerFactory,
                    isDraftMessagesEnabled = isDraftMessagesEnabled,
                )
            } else {
                ChannelListViewModelFactory(
                    filter = filter,
                    sort = sort,
                    limit = limit,
                    messageLimit = messageLimit,
                    memberLimit = memberLimit,
                    chatEventHandlerFactory = chatEventHandlerFactory,
                    isDraftMessagesEnabled = isDraftMessagesEnabled,
                )
            }
        }
    }
}
