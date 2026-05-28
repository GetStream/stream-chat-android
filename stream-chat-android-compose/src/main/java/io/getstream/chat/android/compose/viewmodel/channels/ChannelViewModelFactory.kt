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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel.QueryMode
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.state.event.handler.chat.ChatEventHandler
import io.getstream.chat.android.state.event.handler.chat.factory.ChatEventHandlerFactory
import io.getstream.chat.android.state.event.handler.grouped.internal.groupAwareChatEventHandlerFactory
import io.getstream.chat.android.state.extensions.globalStateFlow

/**
 * Builds the factory that contains all the dependencies required for the Channels Screen.
 * It currently provides the [ChannelListViewModel] using those dependencies.
 *
 * Three construction modes are supported:
 * - **Standard**: filter + sort drive a classical `queryChannels` request.
 * - **Predefined**: a `predefinedFilterName` resolved server-side, with optional interpolation values.
 * - **Grouped**: a `groupKey` identifies a state populated by `queryGroupedChannels` responses.
 *
 * Pick the constructor that matches the mode you want — the three identity flavors are mutually
 * exclusive.
 */
@Suppress("LongParameterList")
public class ChannelViewModelFactory internal constructor(
    private val chatClient: ChatClient,
    private val mode: QueryMode,
    private val channelLimit: Int,
    private val memberLimit: Int?,
    private val messageLimit: Int?,
    private val chatEventHandlerFactory: ChatEventHandlerFactory,
    private val isDraftMessageEnabled: Boolean,
    private val messageSearchSort: QuerySorter<Message>?,
) : ViewModelProvider.Factory {

    /**
     * Builds a factory for a [ChannelListViewModel] that queries channels by an explicit filter and sort.
     *
     * @param chatClient The client used to fetch data.
     * @param querySort The sorting order for channels.
     * @param filters The base filters used to filter out channels. When `null`, a default filter scoped
     * to "messaging" channels the current user is a member of is used.
     * @param channelLimit How many channels we fetch per page.
     * @param memberLimit How many members are fetched for each channel item when loading channels.
     * When `null`, the server-side default is used.
     * @param messageLimit How many messages are fetched for each channel item when loading channels.
     * When `null`, the server-side default is used.
     * @param chatEventHandlerFactory The instance of [ChatEventHandlerFactory] used to create [ChatEventHandler].
     * @param isDraftMessageEnabled If the draft message feature is enabled.
     * @param messageSearchSort Optional sorting for message search results.
     * When `null`, the server-side default is used.
     */
    @JvmOverloads
    public constructor(
        chatClient: ChatClient = ChatClient.instance(),
        querySort: QuerySorter<Channel> = QuerySortByField.descByName("last_updated"),
        filters: FilterObject? = null,
        channelLimit: Int = ChannelListViewModel.DEFAULT_CHANNEL_LIMIT,
        memberLimit: Int? = null,
        messageLimit: Int? = null,
        chatEventHandlerFactory: ChatEventHandlerFactory = ChatEventHandlerFactory(chatClient.clientState),
        isDraftMessageEnabled: Boolean = false,
        messageSearchSort: QuerySorter<Message>? = null,
    ) : this(
        chatClient = chatClient,
        mode = QueryMode.Standard(initialFilter = filters, initialSort = querySort),
        channelLimit = channelLimit,
        memberLimit = memberLimit,
        messageLimit = messageLimit,
        chatEventHandlerFactory = chatEventHandlerFactory,
        isDraftMessageEnabled = isDraftMessageEnabled,
        messageSearchSort = messageSearchSort,
    )

    /**
     * Builds a factory for a [ChannelListViewModel] that queries channels using a predefined filter
     * resolved by the server.
     *
     * @param predefinedFilterName The name of the predefined filter registered on the backend.
     * @param filterValues Optional values interpolated into the predefined filter template.
     * @param sortValues Optional values interpolated into the predefined sort template.
     */
    // TODO: restore @JvmOverloads once we resolve the JVM signature clash between the
    //  Predefined `(ChatClient, String)` and Grouped `(ChatClient, String)` overloads — pick a
    //  disambiguator (marker class, different param order, or named factory methods).
    public constructor(
        chatClient: ChatClient = ChatClient.instance(),
        predefinedFilterName: String,
        filterValues: Map<String, Any>? = null,
        sortValues: Map<String, Any>? = null,
        channelLimit: Int = ChannelListViewModel.DEFAULT_CHANNEL_LIMIT,
        memberLimit: Int? = null,
        messageLimit: Int? = null,
        chatEventHandlerFactory: ChatEventHandlerFactory = ChatEventHandlerFactory(chatClient.clientState),
        isDraftMessageEnabled: Boolean = false,
        messageSearchSort: QuerySorter<Message>? = null,
    ) : this(
        chatClient = chatClient,
        mode = QueryMode.Predefined(
            name = predefinedFilterName,
            filterValues = filterValues,
            sortValues = sortValues,
        ),
        channelLimit = channelLimit,
        memberLimit = memberLimit,
        messageLimit = messageLimit,
        chatEventHandlerFactory = chatEventHandlerFactory,
        isDraftMessageEnabled = isDraftMessageEnabled,
        messageSearchSort = messageSearchSort,
    )

    /**
     * Grouped [ChannelListViewModel] factory. Wires the ViewModel to the state identified by
     * [groupKey] without firing a remote call; `queryGroupedChannels` responses populate it.
     *
     * Internally builds a group-aware [ChatEventHandlerFactory] keyed on [groupKey] so that
     * `channel.updated` and channel-add events route channels into the correct group.
     *
     * @param chatClient The client used to fetch data.
     * @param groupKey Identifies the group whose state this ViewModel observes.
     * @param isDraftMessageEnabled If the draft message feature is enabled.
     * @param messageSearchSort Optional sorting for message search results.
     */
    // TODO: see TODO on the Predefined factory ctor — same JVM signature clash story.
    public constructor(
        chatClient: ChatClient = ChatClient.instance(),
        groupKey: String,
        isDraftMessageEnabled: Boolean = false,
        messageSearchSort: QuerySorter<Message>? = null,
    ) : this(
        chatClient = chatClient,
        mode = QueryMode.Grouped(groupKey),
        channelLimit = ChannelListViewModel.DEFAULT_CHANNEL_LIMIT,
        memberLimit = null,
        messageLimit = null,
        chatEventHandlerFactory = groupAwareChatEventHandlerFactory(
            groupKey = groupKey,
            clientState = chatClient.clientState,
        ),
        isDraftMessageEnabled = isDraftMessageEnabled,
        messageSearchSort = messageSearchSort,
    )

    /**
     * Create a new instance of [ChannelListViewModel] class.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ChannelListViewModel::class.java) {
            "ChannelViewModelFactory can only create instances of ChannelListViewModel"
        }
        @Suppress("UNCHECKED_CAST")
        return ChannelListViewModel(
            chatClient = chatClient,
            mode = mode,
            channelLimit = channelLimit,
            memberLimit = memberLimit,
            messageLimit = messageLimit,
            chatEventHandlerFactory = chatEventHandlerFactory,
            searchDebounceMs = ChannelListViewModel.SEARCH_DEBOUNCE_MS,
            isDraftMessageEnabled = isDraftMessageEnabled,
            messageSearchSort = messageSearchSort,
            globalState = chatClient.globalStateFlow,
        ) as T
    }
}
