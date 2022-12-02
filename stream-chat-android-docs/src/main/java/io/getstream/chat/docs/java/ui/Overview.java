package io.getstream.chat.docs.java.ui;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import io.getstream.chat.android.ui.feature.messages.list.MessageListView;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelBinding;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelFactory;

/**
 * [Overview](https://getstream.io/chat/docs/sdk/android/ui/overview/)
 */
public class Overview extends Fragment {

    private MessageListView messageListView;

    /**
     * [ViewModels](https://getstream.io/chat/docs/sdk/android/ui/overview/#viewmodels)
     */
    public void viewModels() {
        // 1
        ViewModelProvider.Factory factory = new MessageListViewModelFactory
                .Builder()
                .cid("messaging:123")
                .build();
        ViewModelProvider provider = new ViewModelProvider(this, factory);
        // 2
        MessageListViewModel viewModel = provider.get(MessageListViewModel.class);
        // 3
        MessageListViewModelBinding.bind(viewModel, messageListView, getViewLifecycleOwner());
    }
}
