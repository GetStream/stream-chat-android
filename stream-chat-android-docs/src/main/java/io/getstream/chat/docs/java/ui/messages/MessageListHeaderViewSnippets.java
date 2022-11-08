package io.getstream.chat.docs.java.ui.messages;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import io.getstream.chat.android.ui.feature.messages.list.header.MessageListHeaderView;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListHeaderViewModel;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListHeaderViewModelBinding;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelFactory;

/**
 * [Message List Header](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list-header/)
 */
public class MessageListHeaderViewSnippets extends Fragment {

    private MessageListHeaderView messageListHeaderView;

    public void usage() {
        // Get ViewModel
        ViewModelProvider.Factory factory = new MessageListViewModelFactory.Builder()
                .cid("channelType:channelId")
                .build();
        ViewModelProvider provider = new ViewModelProvider(this, factory);
        MessageListHeaderViewModel viewModel = provider.get(MessageListHeaderViewModel.class);
        // Bind it with MessageListHeaderView
        MessageListHeaderViewModelBinding.bind(viewModel, messageListHeaderView, getViewLifecycleOwner());
    }

    public void handlingActions() {
        messageListHeaderView.setBackButtonClickListener(() -> {
            // Handle back button click
        });
        messageListHeaderView.setAvatarClickListener(() -> {
            // Handle avatar click
        });
        messageListHeaderView.setTitleClickListener(() -> {
            // Handle title click
        });
        messageListHeaderView.setSubtitleClickListener(() -> {
            // Handle subtitle click
        });
    }
}
