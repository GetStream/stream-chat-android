package com.getstream.sdk.chat.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.getstream.sdk.chat.viewmodel.channels.ChannelsViewModel
import com.getstream.sdk.chat.viewmodel.channels.ChannelsViewModelImpl
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.livedata.ChatDomain

@Suppress("UNCHECKED_CAST")
/**
 * Creates a channels view model factory
 *
 * @param filter how to filter the channels
 * @param sort how to sort the channels, defaults to last_updated
 * @param limit how many channels to return
 *
 * @see Filters
 * @see QuerySort
 */
public class ChannelsViewModelFactory(
    private val filter: FilterObject = Filters.and(
        Filters.eq("type", "messaging"),
        Filters.`in`("members", listOf(ChatDomain.instance().currentUser.id))
    ),
    private val sort: QuerySort<Channel> = ChannelsViewModel.DEFAULT_SORT,
    private val limit: Int = 30
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        ChannelsViewModelImpl(ChatDomain.instance(), filter, sort, limit) as T
}
