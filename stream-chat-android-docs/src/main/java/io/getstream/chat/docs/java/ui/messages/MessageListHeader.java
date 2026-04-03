package io.getstream.chat.docs.java.ui.messages;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import io.getstream.chat.android.ui.feature.messages.header.ChannelHeaderView;
import io.getstream.chat.android.ui.viewmodel.messages.ChannelHeaderViewModel;
import io.getstream.chat.android.ui.viewmodel.messages.ChannelHeaderViewModelBinding;
import io.getstream.chat.android.ui.viewmodel.messages.ChannelViewModelFactory;

/**
 * [Message List Header](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list-header/)
 */
public class MessageListHeader extends Fragment {

    private ChannelHeaderView channelHeaderView;

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list-header/#usage)
     */
    public void usage() {
        // Initialize ViewModel
        ViewModelProvider.Factory factory = new ChannelViewModelFactory.Builder(requireContext())
                .cid("messaging:123")
                .build();
        ViewModelProvider provider = new ViewModelProvider(this, factory);
        ChannelHeaderViewModel viewModel = provider.get(ChannelHeaderViewModel.class);

        // Bind the View and ViewModel
        ChannelHeaderViewModelBinding.bind(viewModel, channelHeaderView, getViewLifecycleOwner());
    }

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list-header/#handling-actions)
     */
    public void handlingActions() {
        channelHeaderView.setBackButtonClickListener(() -> {
            // Handle back button click
        });
        channelHeaderView.setAvatarClickListener(() -> {
            // Handle avatar click
        });
        channelHeaderView.setTitleClickListener(() -> {
            // Handle title click
        });
        channelHeaderView.setSubtitleClickListener(() -> {
            // Handle subtitle click
        });
    }
}
