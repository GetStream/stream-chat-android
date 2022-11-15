package io.getstream.chat.docs.java.ui.guides;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Collections;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.models.FilterObject;
import io.getstream.chat.android.models.Filters;
import io.getstream.chat.android.models.querysort.QuerySortByField;
import io.getstream.chat.android.ui.feature.channels.header.ChannelListHeaderView;
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView;
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListHeaderViewModel;
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListHeaderViewModelBinding;
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModel;
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModelBinding;
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModelFactory;

/**
 * [Building A Channel List Screen](https://getstream.io/chat/docs/sdk/android/ui/guides/building-channel-list-screen/)
 */
public class BuildingAChannelListScreen extends Fragment {

    private ChannelListHeaderView channelListHeaderView;
    private ChannelListView channelListView;

    public void create() {
        ChannelListHeaderViewModel channelListHeaderViewModel = new ViewModelProvider(this).get(ChannelListHeaderViewModel.class);

        FilterObject filter = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.in("members", Collections.singletonList(ChatClient.instance().getCurrentUser().getId()))
        );

        ViewModelProvider.Factory factory = new ChannelListViewModelFactory.Builder()
                .filter(filter)
                .sort(QuerySortByField.descByName("lastUpdated"))
                .limit(30)
                .build();

        ChannelListViewModel channelListViewModel = new ViewModelProvider(this, factory).get(ChannelListViewModel.class);
    }

    public void bind(ChannelListHeaderViewModel channelListHeaderViewModel, ChannelListViewModel channelListViewModel) {
        ChannelListHeaderViewModelBinding.bind(channelListHeaderViewModel, channelListHeaderView, getViewLifecycleOwner());
        ChannelListViewModelBinding.bind(channelListViewModel, channelListView, getViewLifecycleOwner());
    }
}
