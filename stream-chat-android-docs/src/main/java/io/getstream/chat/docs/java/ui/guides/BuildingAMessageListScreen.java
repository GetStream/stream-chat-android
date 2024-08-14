package io.getstream.chat.docs.java.ui.guides;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import io.getstream.chat.android.models.Message;
import io.getstream.chat.android.ui.common.state.messages.Edit;
import io.getstream.chat.android.ui.common.state.messages.MessageMode;
import io.getstream.chat.android.ui.common.state.messages.Reply;
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView;
import io.getstream.chat.android.ui.feature.messages.header.MessageListHeaderView;
import io.getstream.chat.android.ui.feature.messages.list.MessageListView;
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModel;
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelBinding;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListHeaderViewModel;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListHeaderViewModelBinding;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelBinding;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelFactory;

/**
 * [Building A Message List Screen](https://getstream.io/chat/docs/sdk/android/ui/guides/building-message-list-screen/)
 */
public class BuildingAMessageListScreen extends Fragment {

    private MessageListView messageListView;
    private MessageListHeaderView messageListHeaderView;
    private MessageComposerView messageComposerView;

    public void usage() {
        // Create ViewModels for the Views
        ViewModelProvider.Factory factory = new MessageListViewModelFactory.Builder(requireContext())
                .cid("messaging:123")
                .build();
        ViewModelProvider provider = new ViewModelProvider(this, factory);
        MessageListHeaderViewModel messageListHeaderViewModel = provider.get(MessageListHeaderViewModel.class);
        MessageListViewModel messageListViewModel = provider.get(MessageListViewModel.class);
        MessageComposerViewModel messageComposerViewModel = provider.get(MessageComposerViewModel.class);

        // Bind the ViewModels with the Views
        MessageListHeaderViewModelBinding.bind(messageListHeaderViewModel, messageListHeaderView, getViewLifecycleOwner());
        MessageListViewModelBinding.bind(messageListViewModel, messageListView, getViewLifecycleOwner());
        MessageComposerViewModelBinding.bind(messageComposerViewModel, messageComposerView, getViewLifecycleOwner());

        // Let both message list header and message input know when we open a thread
        messageListViewModel.getMode().observe(getViewLifecycleOwner(), mode -> {
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
        messageListViewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof MessageListViewModel.State.NavigateUp) {
                requireActivity().finish();
            }
        });

        // Handle back button behaviour correctly when you're in a thread
        MessageListHeaderView.OnClickListener backHandler = () -> {
            messageListViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed.INSTANCE);
        };
        messageListHeaderView.setBackButtonClickListener(backHandler);

        // Override the default Activity's back button behaviour
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backHandler.onClick();
            }
        });
    }
}
