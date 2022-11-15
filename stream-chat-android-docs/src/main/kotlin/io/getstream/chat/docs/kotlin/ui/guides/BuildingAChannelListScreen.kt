package io.getstream.chat.docs.kotlin.ui.guides

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.querysort.QuerySortByField
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.ui.feature.channels.header.ChannelListHeaderView
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListHeaderViewModel
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.channels.bindView

/**
 * [Building A Channel List Screen](https://getstream.io/chat/docs/sdk/android/ui/guides/building-channel-list-screen/)
 */
class BuildingAChannelListScreen : Fragment() {

    private lateinit var channelListHeaderView: ChannelListHeaderView
    private lateinit var channelListView: ChannelListView

    fun create() {
        val channelListHeaderViewModel: ChannelListHeaderViewModel by viewModels()

        val channelListFactory: ChannelListViewModelFactory = ChannelListViewModelFactory(
            filter = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.`in`("members", listOf(ChatClient.instance().getCurrentUser()!!.id)),
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
