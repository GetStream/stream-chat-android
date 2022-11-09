package io.getstream.chat.docs.java.ui.channels;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import io.getstream.chat.android.ui.feature.channels.header.ChannelListHeaderView;
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListHeaderViewModel;
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListHeaderViewModelBinding;

/**
 * [Channel List Header](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list-header/)
 */
public class ChannelListHeaderViewSnippets extends Fragment {

    private ChannelListHeaderView channelListHeaderView;

    public void usage() {
        // Get ViewModel
        ChannelListHeaderViewModel viewModel = new ViewModelProvider(this).get(ChannelListHeaderViewModel.class);

        // Bind it with ChannelListHeaderView
        ChannelListHeaderViewModelBinding.bind(viewModel, channelListHeaderView, getViewLifecycleOwner());
    }

    public void handlingActions() {
        channelListHeaderView.setOnActionButtonClickListener(() -> {
            // Handle action button click
        });
        channelListHeaderView.setOnUserAvatarClickListener(() -> {
            // Handle user avatar click
        });
    }
}
