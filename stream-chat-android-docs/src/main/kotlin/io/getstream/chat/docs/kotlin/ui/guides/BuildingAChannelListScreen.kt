package io.getstream.chat.docs.kotlin.ui.guides

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.querysort.QuerySortByField
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel
import io.getstream.chat.android.ui.channel.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory

/**
 * [Building A Channel List Screen](https://getstream.io/chat/docs/sdk/android/ui/guides/building-channel-list-screen/)
 */
class BuildingAChannelListScreen: Fragment() {

    private lateinit var channelListHeaderView: ChannelListHeaderView
    private lateinit var channelListView: ChannelListView

    fun create() {
        val channelListHeaderViewModel: ChannelListHeaderViewModel by viewModels()

        val channelListFactory: ChannelListViewModelFactory = ChannelListViewModelFactory(
            filter = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.`in`("members", listOf(ChatClient.instance().getCurrentUserId()!!)),
            ),
            sort = QuerySortByField.descByName("lastUpdated"),
            limit = 30,
        )
        val channelListViewModel: ChannelListViewModel by viewModels { channelListFactory }
    }

    fun bind(channelListHeaderViewModel: ChannelListHeaderViewModel, channelListViewModel: ChannelListViewModel) {
        channelListHeaderViewModel.bindView(channelListHeaderView, viewLifecycleOwner)
        channelListViewModel.bindView(channelListView, viewLifecycleOwner)
    }
}
