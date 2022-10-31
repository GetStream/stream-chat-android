package io.getstream.chat.docs.java.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel;

import io.getstream.chat.android.ui.message.list.MessageListView;
import io.getstream.chat.android.ui.message.list.viewmodel.MessageListViewModelBinding;
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory;

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
