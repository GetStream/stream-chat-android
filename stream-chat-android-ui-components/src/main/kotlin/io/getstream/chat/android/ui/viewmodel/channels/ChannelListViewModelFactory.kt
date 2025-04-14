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

/**
 * Creates a channels view model factory.
 *
 * @param filter How to filter the channels.
 * @param sort How to sort the channels, defaults to last_updated.
 * @param limit How many channels to return.
 * @param memberLimit The number of members per channel.
 * @param messageLimit The number of messages to fetch for each channel.
 * @param isDraftMessagesEnabled Enables or disables draft messages.
 * @param chatEventHandlerFactory The instance of [ChatEventHandlerFactory] that will be used to create [ChatEventHandler].
 *
 * @see Filters
 * @see QuerySorter
 */
public class ChannelListViewModelFactory @JvmOverloads constructor(
    private val filter: FilterObject? = null,
    private val sort: QuerySorter<Channel> = ChannelListViewModel.DEFAULT_SORT,
    private val limit: Int = ChannelListViewModel.DEFAULT_CHANNEL_LIMIT,
    private val messageLimit: Int = ChannelListViewModel.DEFAULT_MESSAGE_LIMIT,
    private val memberLimit: Int = ChannelListViewModel.DEFAULT_MEMBER_LIMIT,
    private val isDraftMessagesEnabled: Boolean = ChatUI.draftMessagesEnabled,
    private val chatEventHandlerFactory: ChatEventHandlerFactory = ChatEventHandlerFactory(),
) : ViewModelProvider.Factory {

    /**
     * Returns an instance of [ChannelListViewModel].
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ChannelListViewModel::class.java) {
            "ChannelListViewModelFactory can only create instances of ChannelListViewModel"
        }

        @Suppress("UNCHECKED_CAST")
        return ChannelListViewModel(
            filter = filter,
            sort = sort,
            limit = limit,
            messageLimit = messageLimit,
            memberLimit = memberLimit,
            chatEventHandlerFactory = chatEventHandlerFactory,
            isDraftMessagesEnabled = isDraftMessagesEnabled,
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
        private var limit: Int = ChannelListViewModel.DEFAULT_CHANNEL_LIMIT
        private var messageLimit: Int = ChannelListViewModel.DEFAULT_MESSAGE_LIMIT
        private var memberLimit: Int = ChannelListViewModel.DEFAULT_MEMBER_LIMIT
        private var chatEventHandlerFactory: ChatEventHandlerFactory = ChatEventHandlerFactory()
        private var isDraftMessagesEnabled: Boolean = ChatUI.draftMessagesEnabled

        /**
         * Sets the way to filter the channels.
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
         * Sets the number of channels to return.
         */
        public fun limit(limit: Int): Builder = apply {
            this.limit = limit
        }

        /**
         * Sets the number of messages to fetch for each channel.
         */
        public fun messageLimit(messageLimit: Int): Builder = apply {
            this.messageLimit = messageLimit
        }

        /**
         * Sets the number of members per channel.
         */
        public fun memberLimit(memberLimit: Int): Builder = apply {
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
         */
        public fun build(): ViewModelProvider.Factory {
            return ChannelListViewModelFactory(
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
