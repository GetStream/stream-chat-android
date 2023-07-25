package io.getstream.chat.docs.java.ui.guides;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.models.Message;
import io.getstream.chat.android.ui.common.state.messages.MessageMode;
import io.getstream.chat.android.ui.feature.messages.list.MessageListView;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel;
import io.getstream.chat.docs.databinding.ViewCustomMessageComposerBinding;

/**
 * [Creating a Custom Message Composer](https://getstream.io/chat/docs/sdk/android/ui/guides/custom-message-composer/)
 */
public class CreatingCustomMessageComposer {

    /**
     * [Sending and Editing Messages](https://getstream.io/chat/docs/sdk/android/ui/guides/custom-message-composer/#sending-and-editing-messages)
     */
    class SendingAndEditingMessages {

        private CustomMessageComposerView customMessageComposerView;
        private MessageListView messageListView;
        private String cid;

        class CustomMessageComposerView extends ConstraintLayout {

            private ViewCustomMessageComposerBinding binding = ViewCustomMessageComposerBinding
                    .inflate(LayoutInflater.from(getContext()), this);

            private ChannelClient channelClient;

            private Message messageToEdit;

            public CustomMessageComposerView(@NonNull Context context) {
                this(context, null);
            }

            public CustomMessageComposerView(@NonNull Context context, @Nullable AttributeSet attrs) {
                this(context, attrs, 0);
            }

            public CustomMessageComposerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
                super(context, attrs, defStyleAttr);
                init();
            }

            private void init() {
                binding.sendButton.setOnClickListener(v -> {
                    String text = binding.inputField.getText().toString();

                    if (messageToEdit != null) {
                        Message message = messageToEdit.newBuilder()
                                .withText(text)
                                .build();
                        channelClient.updateMessage(message).enqueue();
                    } else {
                        Message message = new Message.Builder()
                                .withText(text)
                                .withParentId(null)
                                .build();
                        channelClient.sendMessage(message).enqueue();
                    }

                    messageToEdit = null;
                    binding.inputField.setText("");
                });
            }

            public void setChannelClient(ChannelClient channelClient) {
                this.channelClient = channelClient;
            }

            public void editMessage(Message message) {
                this.messageToEdit = message;
                binding.inputField.setText(message.getText());
            }
        }

        private void usingCustomMessageComposerView() {
            customMessageComposerView.setChannelClient(ChatClient.instance().channel(cid));

            messageListView.setMessageEditHandler(customMessageComposerView::editMessage);
        }
    }

    /**
     * [Handling Threads](https://getstream.io/chat/docs/sdk/android/ui/guides/custom-message-composer/#handling-threads)
     */
    class HandlingThreads extends Fragment {

        private CustomMessageComposerView customMessageComposerView;
        private MessageListView messageListView;
        private MessageListViewModel messageListViewModel;
        private String cid;

        class CustomMessageComposerView extends ConstraintLayout {

            private ViewCustomMessageComposerBinding binding = ViewCustomMessageComposerBinding
                    .inflate(LayoutInflater.from(getContext()), this);

            private ChannelClient channelClient;

            private Message messageToEdit;
            private Message parentMessage;

            public CustomMessageComposerView(@NonNull Context context) {
                this(context, null);
            }

            public CustomMessageComposerView(@NonNull Context context, @Nullable AttributeSet attrs) {
                this(context, attrs, 0);
            }

            public CustomMessageComposerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
                super(context, attrs, defStyleAttr);
                init();
            }

            private void init() {
                binding.sendButton.setOnClickListener(v -> {
                    String text = binding.inputField.getText().toString();

                    if (messageToEdit != null) {
                        Message message = messageToEdit.newBuilder()
                                .withText(text)
                                .build();
                        channelClient.updateMessage(message).enqueue();
                    } else {
                        Message message = new Message.Builder()
                                .withText(text)
                                .withParentId(parentMessage != null ? parentMessage.getId() : null)
                                .build();
                        channelClient.sendMessage(message).enqueue();
                    }

                    messageToEdit = null;
                    binding.inputField.setText("");
                });
            }

            public void setChannelClient(ChannelClient channelClient) {
                this.channelClient = channelClient;
            }

            public void editMessage(Message message) {
                this.messageToEdit = message;
                binding.inputField.setText(message.getText());
            }

            public void setActiveThread(Message parentMessage) {
                this.parentMessage = parentMessage;
                this.messageToEdit = null;
                binding.inputField.setText("");
            }

            public void resetThread() {
                this.parentMessage = null;
                this.messageToEdit = null;
                binding.inputField.setText("");
            }
        }

        private void handlingThreads() {
            customMessageComposerView.setChannelClient(ChatClient.instance().channel(cid));

            messageListView.setMessageEditHandler(customMessageComposerView::editMessage);

            messageListViewModel.getMode().observe(getViewLifecycleOwner(), mode -> {
                if (mode instanceof MessageMode.MessageThread) {
                    MessageMode.MessageThread messageThread = (MessageMode.MessageThread) mode;
                    customMessageComposerView.setActiveThread(messageThread.getParentMessage());
                } else if (mode instanceof MessageMode.Normal) {
                    customMessageComposerView.resetThread();
                }
            });
        }
    }
}
