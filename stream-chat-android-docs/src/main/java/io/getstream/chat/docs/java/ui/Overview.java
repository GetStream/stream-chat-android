package io.getstream.chat.docs.java.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import io.getstream.chat.android.ui.feature.messages.list.MessageListView;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelBinding;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelFactory;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel;

/**
 * [Overview](https://getstream.io/chat/docs/sdk/android/ui/overview/)
 */
public class Overview extends AppCompatActivity {

    public void viewModelsInitialization(MessageListView messageListView, LifecycleOwner viewLifecycleOwner) {
        ViewModelProvider.Factory factory = new MessageListViewModelFactory // 1
                .Builder()
                .cid("channelType:channelId")
                .build();
        ViewModelProvider provider = new ViewModelProvider(this, factory);
        MessageListViewModel messageListViewModel = provider.get(MessageListViewModel.class); // 2

        MessageListViewModelBinding.bind(messageListViewModel, messageListView, viewLifecycleOwner); // 3
    }
}
