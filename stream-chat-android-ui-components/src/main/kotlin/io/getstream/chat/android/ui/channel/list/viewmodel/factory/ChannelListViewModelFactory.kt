package io.getstream.chat.android.ui.channel.list.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.offline.event.handler.ChatEventHandler
import io.getstream.chat.android.offline.event.handler.factory.ChatEventHandlerFactory
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel

/**
 * Creates a channels view model factory.
 *
 * @param filter How to filter the channels.
 * @param sort How to sort the channels, defaults to last_updated.
 * @param limit How many channels to return.
 * @param memberLimit The number of members per channel.
 * @param messageLimit The number of messages to fetch for each channel.
 * @param chatEventHandlerFactory The instance of [ChatEventHandlerFactory] that will be used to create [ChatEventHandler].
 *
 * @see Filters
 * @see QuerySort
 */
public class ChannelListViewModelFactory @JvmOverloads constructor(
    private val filter: FilterObject? = null,
    private val sort: QuerySort<Channel> = ChannelListViewModel.DEFAULT_SORT,
    private val limit: Int = 30,
    private val messageLimit: Int = 1,
    private val memberLimit: Int = 30,
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
        ) as T
    }
}
