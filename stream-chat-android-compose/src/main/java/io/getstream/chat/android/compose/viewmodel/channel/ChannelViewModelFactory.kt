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
) : ViewModelProvider.Factory {

    private val factories: Map<Class<*>, () -> ViewModel> = mapOf(
        ChannelListViewModel::class.java to {
            ChannelListViewModel(chatClient, chatDomain, querySort, filters)
        }
    )

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: ViewModel = factories[modelClass]?.invoke()
            ?: throw IllegalArgumentException("MessageListViewModelFactory can only create instances of the following classes: ${factories.keys.joinToString { it.simpleName }}")

        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}
