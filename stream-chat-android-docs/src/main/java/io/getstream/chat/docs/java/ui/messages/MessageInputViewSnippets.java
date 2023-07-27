package io.getstream.chat.docs.java.ui.messages;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.getstream.sdk.chat.utils.typing.TypingUpdatesBuffer;
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel;

import java.io.File;
import java.util.List;

import io.getstream.chat.android.client.models.Attachment;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.ui.TransformStyle;
import io.getstream.chat.android.ui.message.input.MessageInputView;
import io.getstream.chat.android.ui.message.input.transliteration.DefaultStreamTransliterator;
import io.getstream.chat.android.ui.message.input.viewmodel.MessageInputViewModelBinding;
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory;
import io.getstream.chat.android.ui.suggestion.list.adapter.SuggestionListItem;
import io.getstream.chat.android.ui.suggestion.list.adapter.SuggestionListItemViewHolderFactory;
import io.getstream.chat.android.ui.suggestion.list.adapter.viewholder.BaseSuggestionItemViewHolder;
import io.getstream.chat.docs.R;
import io.getstream.chat.docs.databinding.ItemCommandBinding;
import kotlin.Pair;

/**
 * [Message Input](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-input/)
 */
public class MessageInputViewSnippets extends Fragment {

    private MessageInputView messageInputView;

    public void usage() {
        // Init view model
        ViewModelProvider.Factory factory = new MessageListViewModelFactory.Builder()
                .cid("messaging:123")
                .build();
        ViewModelProvider provider = new ViewModelProvider(this, factory);
        MessageInputViewModel viewModel = provider.get(MessageInputViewModel.class);

        // Bind view and viewModel
        MessageInputViewModelBinding.bind(viewModel, messageInputView, getViewLifecycleOwner());
    }

    public void handlingActions() {
        messageInputView.setOnSendButtonClickListener(() -> {
            // Handle send button click
        });

        messageInputView.setTypingUpdatesBuffer(new TypingUpdatesBuffer() {
            @Override
            public void onKeystroke(@NonNull String inputText) {
                // Your custom implementation of TypingUpdatesBuffer
            }

            @Override
            public void clear() {
                // Your custom implementation of TypingUpdatesBuffer
            }
        });

        messageInputView.setMaxMessageLengthHandler((messageText, messageLength, maxMessageLength, maxMessageLengthExceeded) -> {
            if (maxMessageLengthExceeded) {
                // Show custom max-length error
            } else {
                // Hide custom max-length error
            }
        });

        messageInputView.setSendMessageHandler(new MessageInputView.MessageSendHandler() {
            @Override
            public void sendMessage(@NonNull String messageText, @Nullable Message messageReplyTo) {
                // Handle send message
            }

            @Override
            public void sendMessageWithAttachments(@NonNull String message, @NonNull List<? extends Pair<? extends File, String>> attachmentsWithMimeTypes, @Nullable Message messageReplyTo) {
                // Handle message with attachments
            }

            @Override
            public void sendMessageWithCustomAttachments(@NonNull String message, @NonNull List<Attachment> attachments, @Nullable Message messageReplyTo) {
                // Handle message with custom attachments
            }

            @Override
            public void sendToThread(@NonNull Message parentMessage, @NonNull String messageText, boolean alsoSendToChannel) {
                // Handle message to thread
            }

            @Override
            public void sendToThreadWithAttachments(@NonNull Message parentMessage, @NonNull String message, boolean alsoSendToChannel, @NonNull List<? extends Pair<? extends File, String>> attachmentsWithMimeTypes) {
                // Handle message to thread with attachments
            }

            @Override
            public void sendToThreadWithCustomAttachments(@NonNull Message parentMessage, @NonNull String message, boolean alsoSendToChannel, @NonNull List<Attachment> attachmentsWithMimeTypes) {
                // Handle message to thread with custom attachments
            }

            @Override
            public void editMessage(@NonNull Message oldMessage, @NonNull String newMessageText) {
                // Handle edit message
            }

            @Override
            public void dismissReply() {
                // Handle dismiss reply
            }
        });
    }

    public void customization() {
        TransformStyle.setMessageInputStyleTransformer(source -> {
            // Customize the style
            return source;
        });
    }

    public void changingSendMessageButton() {
        messageInputView.setMessageInputModeListener(inputMode -> {
            if (inputMode instanceof MessageInputView.InputMode.Edit) {
                messageInputView.setSendMessageButtonEnabledDrawable(requireContext().getDrawable(R.drawable.stream_ui_ic_check_single));
                messageInputView.setSendMessageButtonDisabledDrawable(requireContext().getDrawable(R.drawable.stream_ui_ic_close));
            } else {
                messageInputView.setSendMessageButtonEnabledDrawable(requireContext().getDrawable(R.drawable.stream_ui_ic_filled_up_arrow));
                messageInputView.setSendMessageButtonDisabledDrawable(requireContext().getDrawable(R.drawable.stream_ui_ic_filled_right_arrow));
            }
        });
    }

    public final class CustomCommandViewHolder extends BaseSuggestionItemViewHolder<SuggestionListItem.CommandItem> {

        ItemCommandBinding binding;

        public CustomCommandViewHolder(ItemCommandBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void bindItem(@NonNull SuggestionListItem.CommandItem item) {
            binding.commandNameTextView.setText(item.getCommand().getName());
        }
    }

    public final class CustomSuggestionListViewHolderFactory extends SuggestionListItemViewHolderFactory {
        @NonNull
        @Override
        public BaseSuggestionItemViewHolder<SuggestionListItem.CommandItem> createCommandViewHolder(@NonNull ViewGroup parentView) {
            return new CustomCommandViewHolder(ItemCommandBinding.inflate(LayoutInflater.from(parentView.getContext()), parentView, false));
        }
    }

    public void customSuggestionItems() {
        messageInputView.setSuggestionListViewHolderFactory(new CustomSuggestionListViewHolderFactory());
    }

    public void changingMentionSearch(List<User> users) {
        MessageInputView.DefaultUserLookupHandler defaultUserLookupHandler = new MessageInputView.DefaultUserLookupHandler(users, new DefaultStreamTransliterator(null));
        messageInputView.setUserLookupHandler(defaultUserLookupHandler);
    }

    public void transliteration(List<User> users) {
        MessageInputView.DefaultUserLookupHandler defaultUserLookupHandler = new MessageInputView.DefaultUserLookupHandler(
                users,
                new DefaultStreamTransliterator("Cyrl-Latn")
        );
        messageInputView.setUserLookupHandler(defaultUserLookupHandler);
    }
}
