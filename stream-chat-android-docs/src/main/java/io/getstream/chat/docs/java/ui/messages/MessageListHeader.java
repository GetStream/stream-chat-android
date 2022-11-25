package io.getstream.chat.docs.java.ui.messages;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import io.getstream.chat.android.ui.feature.messages.header.MessageListHeaderView;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListHeaderViewModel;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListHeaderViewModelBinding;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelFactory;

/**
 * [Message List Header](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list-header/)
 */
public class MessageListHeader extends Fragment {

    private MessageListHeaderView messageListHeaderView;

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list-header/#usage)
     */
    public void usage() {
        // Initialize ViewModel
        ViewModelProvider.Factory factory = new MessageListViewModelFactory.Builder()
                .cid("messaging:123")
                .build();
        ViewModelProvider provider = new ViewModelProvider(this, factory);
        MessageListHeaderViewModel viewModel = provider.get(MessageListHeaderViewModel.class);

        // Bind the View and ViewModel
        MessageListHeaderViewModelBinding.bind(viewModel, messageListHeaderView, getViewLifecycleOwner());
    }

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list-header/#handling-actions)
     */
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
