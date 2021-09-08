package com.getstream.sdk.chat.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.getstream.sdk.chat.viewmodel.channels.ChannelsViewModel
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.livedata.ChatDomain

/**
 * Creates a channels view model factory.
 *
 * @param filter How to filter the channels.
 * @param sort How to sort the channels, defaults to last_updated.
 * @param limit How many channels to return.
 *
 * @see Filters
 * @see QuerySort
 */
public class ChannelsViewModelFactory @JvmOverloads constructor(
    private val filter: FilterObject? = null,
    private val sort: QuerySort<Channel> = ChannelsViewModel.DEFAULT_SORT,
    private val limit: Int = 30
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ChannelsViewModel::class.java) {
            "ChannelsViewModelFactory can only create instances of ChannelsViewModel"
        }

        @Suppress("UNCHECKED_CAST")
        return ChannelsViewModel(ChatDomain.instance(), filter, sort, limit) as T
    }
}
