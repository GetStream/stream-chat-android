package io.getstream.chat.docs.java.ui.guides;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel;
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Mode.Normal;
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Mode.Thread;

import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.common.state.Edit;
import io.getstream.chat.android.common.state.MessageMode;
import io.getstream.chat.android.common.state.Reply;
import io.getstream.chat.android.ui.message.composer.MessageComposerView;
import io.getstream.chat.android.ui.message.composer.viewmodel.MessageComposerViewModel;
import io.getstream.chat.android.ui.message.composer.viewmodel.MessageComposerViewModelBinding;

import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.common.state.MessageMode;
import io.getstream.chat.android.ui.message.input.MessageInputView;
import io.getstream.chat.android.ui.message.input.viewmodel.MessageInputViewModelBinding;
import io.getstream.chat.android.ui.message.list.MessageListView;
import io.getstream.chat.android.ui.message.list.header.MessageListHeaderView;
import io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModel;
import io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModelBinding;
import io.getstream.chat.android.ui.message.list.viewmodel.MessageListViewModelBinding;
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory;

/**
 * [Building A Message List Screen](https://getstream.io/chat/docs/sdk/android/ui/guides/building-message-list-screen/)
 */
public class BuildingAMessageListScreen extends Fragment {

    private MessageListView messageListView;
    private MessageListHeaderView messageListHeaderView;
    private MessageComposerView messageComposerView;

    public void usage() {
        // Create view models
        ViewModelProvider.Factory factory = new MessageListViewModelFactory.Builder()
                .cid("channelType:channelId")
                .build();
        ViewModelProvider provider = new ViewModelProvider(this, factory);
        MessageListHeaderViewModel messageListHeaderViewModel = provider.get(MessageListHeaderViewModel.class);
        MessageListViewModel messageListViewModel = provider.get(MessageListViewModel.class);
        MessageComposerViewModel messageComposerViewModel = provider.get(MessageComposerViewModel.class);

        // Bind view models
        MessageListHeaderViewModelBinding.bind(messageListHeaderViewModel, messageListHeaderView, this);
        MessageListViewModelBinding.bind(messageListViewModel, messageListView, this);
        MessageComposerViewModelBinding.bind(messageComposerViewModel, messageComposerView, this);

        // Let both message list header and message input know when we open a thread
        messageListViewModel.getMode().observe(this, mode -> {
            if (mode instanceof MessageMode.MessageThread) {
                Message parentMessage = ((MessageMode.MessageThread) mode).getParentMessage();
                messageListHeaderViewModel.setActiveThread(parentMessage);
                messageComposerViewModel.setMessageMode(new MessageMode.MessageThread(parentMessage, null));
            } else if (mode instanceof MessageMode.Normal) {
                messageListHeaderViewModel.resetThread();
                messageComposerViewModel.leaveThread();
            }
        });

        // Let the message composer know when we are replying to a message
        messageListView.setMessageReplyHandler((cid, message) ->
                messageComposerViewModel.performMessageAction(new Reply(message))
        );

        // Let the message composer know when we are editing a message
        messageListView.setMessageEditHandler(message ->
                messageComposerViewModel.performMessageAction(new Edit(message))
        );

        // Handle navigate up state
        messageListViewModel.getState().observe(this, state -> {
            if (state instanceof MessageListViewModel.State.NavigateUp) {
                // Handle navigate up
            }
        });

        // Handle back button behaviour correctly when you're in a thread
        MessageListHeaderView.OnClickListener backHandler = () -> {
            messageListViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed.INSTANCE);
        };
        messageListHeaderView.setBackButtonClickListener(backHandler);

        // You should also consider overriding default Activity's back button behaviour
    }
}
