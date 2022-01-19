package io.getstream.chat.android.compose.viewmodel.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.offline.ChatDomain

/**
 * Builds the factory that contains all the dependencies required for the Channels Screen.
 * It currently provides the [ChannelListViewModel] using those dependencies.
 *
 * @param chatClient The client used to fetch data.
 * @param chatDomain The domain used to fetch and persist data.
 * @param querySort The sorting order for channels.
 * @param filters The base filters used to filter out channels.
 * @param channelLimit How many channels we fetch per page.
 * @param memberLimit How many members are fetched for each channel item when loading channels.
 * @param messageLimit How many messages are fetched for each channel item when loading channels.
 */
public class ChannelViewModelFactory(
    private val chatClient: ChatClient,
    private val chatDomain: ChatDomain,
    private val querySort: QuerySort<Channel>,
    private val filters: FilterObject,
    private val channelLimit: Int = ChannelListViewModel.DEFAULT_CHANNEL_LIMIT,
    private val memberLimit: Int = ChannelListViewModel.DEFAULT_MEMBER_LIMIT,
    private val messageLimit: Int = ChannelListViewModel.DEFAULT_MESSAGE_LIMIT,
) : ViewModelProvider.Factory {

    private val factories: Map<Class<*>, () -> ViewModel> = mapOf(
        ChannelListViewModel::class.java to {
            ChannelListViewModel(
                chatClient = chatClient,
                chatDomain = chatDomain,
                initialSort = querySort,
                initialFilters = filters,
                channelLimit = channelLimit,
                messageLimit = messageLimit,
                memberLimit = memberLimit
            )
        }
    )

    /**
     * Create a new instance of [ChannelListViewModel] class.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: ViewModel = factories[modelClass]?.invoke()
            ?: throw IllegalArgumentException("MessageListViewModelFactory can only create instances of the following classes: ${factories.keys.joinToString { it.simpleName }}")

        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}
