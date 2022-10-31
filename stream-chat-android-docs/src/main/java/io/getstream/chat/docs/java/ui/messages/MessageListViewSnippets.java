package io.getstream.chat.docs.java.ui.messages;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.getstream.sdk.chat.adapter.MessageListItem;
import com.getstream.sdk.chat.utils.DateFormatter;
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.getstream.chat.android.ui.TransformStyle;
import io.getstream.chat.android.ui.message.list.MessageListView;
import io.getstream.chat.android.ui.message.list.adapter.BaseMessageItemViewHolder;
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff;
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewHolderFactory;
import io.getstream.chat.android.ui.message.list.viewmodel.MessageListViewModelBinding;
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory;
import io.getstream.chat.docs.databinding.TodayMessageListItemBinding;

/**
 * [Message List](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list/)
 */
public class MessageListViewSnippets extends Fragment {

    private MessageListView messageListView;

    public void usage() {
        // Init view model
        ViewModelProvider.Factory factory = new MessageListViewModelFactory.Builder()
                .cid("messaging:123")
                .build();
        ViewModelProvider provider = new ViewModelProvider(this, factory);
        MessageListViewModel viewModel = provider.get(MessageListViewModel.class);

        // Bind view and viewModel
        boolean enforceUniqueReactions = true;
        MessageListViewModelBinding.bind(viewModel, messageListView, getViewLifecycleOwner(), enforceUniqueReactions);
    }

    public void handlingActions() {
        messageListView.setLastMessageReadHandler(() -> {
            // Handle when last message got read
        });
        messageListView.setEndRegionReachedHandler(() -> {
            // Handle when end region reached
        });
        messageListView.setMessageDeleteHandler((message) -> {
            // Handle when message is going to be deleted
        });
        messageListView.setThreadStartHandler((message) -> {
            // Handle when new thread for message is started
        });
        messageListView.setMessageFlagHandler((message) -> {
            // Handle when message is going to be flagged
        });
        messageListView.setMessagePinHandler((message) -> {
            // Handle when message is going to be pinned
        });
        messageListView.setMessageUnpinHandler((message) -> {
            // Handle when message is going to be unpinned
        });
        messageListView.setGiphySendHandler((message, giphyAction) -> {
            // Handle when some giphyAction is going to be performed
        });
        messageListView.setMessageRetryHandler((message) -> {
            // Handle when some failed message is going to be retried
        });
        messageListView.setMessageReactionHandler((message, reactionType) -> {
            // Handle when some reaction for message is going to be send
        });
        messageListView.setMessageReplyHandler((cid, message) -> {
            // Handle when message is going to be replied in the channel with cid
        });
        messageListView.setAttachmentDownloadHandler((attachmentDownloadCall) -> {
            // Handle when attachment is going to be downloaded
        });
        messageListView.setMessageEditHandler((message) -> {
            // Handle edit message
        });
    }

    public void listeners() {
        messageListView.setMessageClickListener((message) -> {
            // Listen to click on message events
        });
        messageListView.setEnterThreadListener((message) -> {
            // Listen to events when enter thread associated with a message
        });
        messageListView.setAttachmentDownloadClickListener((attachment) -> {
            // Listen to events when download click for an attachment happens
        });
        messageListView.setUserReactionClickListener((message, user, reaction) -> {
            // Listen to clicks on user reactions on the message options overlay
        });
        messageListView.setMessageLongClickListener((message) -> {
            // Handle long click on message
        });
        messageListView.setAttachmentClickListener((message, attachment) -> {
            // Handle long click on attachment
        });
        messageListView.setUserClickListener((user) -> {
            // Handle click on user avatar
        });
    }

    public void customization() {
        TransformStyle.setMessageListItemStyleTransformer(source -> {
            // Customize the theme
            return source;
        });
        TransformStyle.setMessageListStyleTransformer(source -> {
            // Customize the theme
            return source;
        });
    }

    public void channelFeatureFlags() {
        messageListView.setRepliesEnabled(false);
        messageListView.setDeleteMessageEnabled(false);
        messageListView.setEditMessageEnabled(false);
    }

    public void dateFormatter() {
        messageListView.setMessageDateFormatter(
                new DateFormatter() {
                    private final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    private final DateFormat timeFormat = new SimpleDateFormat("HH:mm");

                    public String formatDate(Date date) {
                        // Provide a way to format Date
                        return dateFormat.format(date);
                    }

                    public String formatTime(Date date) {
                        // Provide a way to format Time
                        return timeFormat.format(date);
                    }
                }
        );
    }

    public void customMessagesFilter() {
        String forbiddenWord = "secret";
        messageListView.setMessageListItemPredicate(item -> {
            if (item instanceof MessageListItem.MessageItem) {
                MessageListItem.MessageItem messageItem = (MessageListItem.MessageItem) item;
                return !((MessageListItem.MessageItem) item).getMessage().getText().contains(forbiddenWord);
            }

            return true;
        });
    }

    public void customMessagesView() {

        class TodayViewHolder extends BaseMessageItemViewHolder<MessageListItem.MessageItem> {

            TodayMessageListItemBinding binding;

            public TodayViewHolder(@NonNull ViewGroup parentView, @NonNull TodayMessageListItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            @Override
            public void bindData(@NonNull MessageListItem.MessageItem data, @Nullable MessageListItemPayloadDiff diff) {
                binding.textLabel.setText(data.getMessage().getText());
            }
        }

        class CustomMessageViewHolderFactory extends MessageListItemViewHolderFactory {

            private int TODAY_VIEW_HOLDER_TYPE = 1;

            @Override
            public int getItemViewType(@NonNull MessageListItem item) {
                if (item instanceof MessageListItem.MessageItem) {
                    MessageListItem.MessageItem messageItem = (MessageListItem.MessageItem) item;
                    if (messageItem.isTheirs()
                            && messageItem.getMessage().getAttachments().isEmpty()
                            && isLessThanDayAgo((messageItem.getMessage().getCreatedAt()))) {
                        return TODAY_VIEW_HOLDER_TYPE;
                    }
                }

                return super.getItemViewType(item);

            }

            private boolean isLessThanDayAgo(Date date) {
                if (date == null) return false;
                long dayInMillis = TimeUnit.DAYS.toMillis(1);

                return date.getTime() >= System.currentTimeMillis() - dayInMillis;
            }

            @NonNull
            @Override
            public BaseMessageItemViewHolder<? extends MessageListItem> createViewHolder(@NonNull ViewGroup parentView, int viewType) {
                if (viewType == TODAY_VIEW_HOLDER_TYPE) {
                    return new TodayViewHolder(parentView, TodayMessageListItemBinding.inflate(LayoutInflater.from(parentView.getContext()), parentView, false));
                }
                return super.createViewHolder(parentView, viewType);
            }

            public void setCustomViewHolderFactory() {
                messageListView.setMessageViewHolderFactory(new CustomMessageViewHolderFactory());
            }
        }
    }

    public void customEmptyState() {
        TextView textView = new TextView(getContext());
        textView.setText("There are no messages yet");
        textView.setTextColor(Color.RED);
        messageListView.setEmptyStateView(
                textView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER
                )
        );
    }

    public void avatarPredicate() {
        messageListView.setShowAvatarPredicate((messageItem) -> messageItem.getPositions().contains(MessageListItem.Position.BOTTOM) && messageItem.isTheirs());
    }
}
