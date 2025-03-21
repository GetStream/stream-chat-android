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

package io.getstream.chat.android.compose.viewmodel.channels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.state.event.handler.chat.ChatEventHandler
import io.getstream.chat.android.state.event.handler.chat.factory.ChatEventHandlerFactory

/**
 * Builds the factory that contains all the dependencies required for the Channels Screen.
 * It currently provides the [ChannelListViewModel] using those dependencies.
 *
 * @param chatClient The client used to fetch data.
 * @param querySort The sorting order for channels.
 * @param filters The base filters used to filter out channels.
 * @param channelLimit How many channels we fetch per page.
 * @param memberLimit How many members are fetched for each channel item when loading channels.
 * @param messageLimit How many messages are fetched for each channel item when loading channels.
 * @param chatEventHandlerFactory The instance of [ChatEventHandlerFactory] used to create [ChatEventHandler].
 */
public class ChannelViewModelFactory(
    private val chatClient: ChatClient = ChatClient.instance(),
    private val querySort: QuerySorter<Channel> = QuerySortByField.descByName("last_updated"),
    private val filters: FilterObject? = null,
    private val channelLimit: Int = ChannelListViewModel.DEFAULT_CHANNEL_LIMIT,
    private val memberLimit: Int = ChannelListViewModel.DEFAULT_MEMBER_LIMIT,
    private val messageLimit: Int = ChannelListViewModel.DEFAULT_MESSAGE_LIMIT,
    private val chatEventHandlerFactory: ChatEventHandlerFactory = ChatEventHandlerFactory(chatClient.clientState),
    private val isDraftMessageEnabled: Boolean = false,
) : ViewModelProvider.Factory {

    private val factories: Map<Class<*>, () -> ViewModel> = mapOf(
        ChannelListViewModel::class.java to {
            ChannelListViewModel(
                chatClient = chatClient,
                initialSort = querySort,
                initialFilters = filters,
                channelLimit = channelLimit,
                messageLimit = messageLimit,
                memberLimit = memberLimit,
                chatEventHandlerFactory = chatEventHandlerFactory,
                isDraftMessageEnabled = isDraftMessageEnabled,
            )
        },
    )

    /**
     * Create a new instance of [ChannelListViewModel] class.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: ViewModel = factories[modelClass]?.invoke()
            ?: throw IllegalArgumentException(
                "MessageListViewModelFactory can only create instances of the " +
                    "following classes: ${factories.keys.joinToString { it.simpleName }}",
            )

        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}
