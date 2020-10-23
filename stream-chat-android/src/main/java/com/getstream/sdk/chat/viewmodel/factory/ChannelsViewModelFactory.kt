package com.getstream.sdk.chat.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.getstream.sdk.chat.viewmodel.channels.ChannelsViewModel
import com.getstream.sdk.chat.viewmodel.channels.ChannelsViewModelImpl
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.livedata.ChatDomain

@Suppress("UNCHECKED_CAST")
public class ChannelsViewModelFactory(
    private val filter: FilterObject = Filters.and(
        Filters.eq("type", "messaging"),
        Filters.`in`("members", listOf(ChatDomain.instance().currentUser.id))
    ),
    private val sort: QuerySort = ChannelsViewModel.DEFAULT_SORT
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        ChannelsViewModelImpl(ChatDomain.instance(), filter, sort) as T
}
