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
 */
public class ChannelViewModelFactory(
    private val chatClient: ChatClient,
    private val chatDomain: ChatDomain,
    private val querySort: QuerySort<Channel>,
    private val filters: FilterObject,
    private val channelLimit: Int = DEFAULT_CHANNEL_LIMIT,
    private val memberLimit: Int = DEFAULT_MEMBER_LIMIT,
    private val messageLimit: Int = DEFAULT_MESSAGE_LIMIT,
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

    internal companion object {
        /**
         * Default value of number of channels to return when querying channels.
         */
        internal const val DEFAULT_CHANNEL_LIMIT = 30

        /**
         * Default value of the number of messages to include in each channel when querying channels.
         */
        internal const val DEFAULT_MESSAGE_LIMIT = 1

        /**
         * Default value of the number of members to include in each channel when querying channels.
         */
        internal const val DEFAULT_MEMBER_LIMIT = 30
    }
}
