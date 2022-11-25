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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.getstream.chat.android.ui.helper.TransformStyle;
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem;
import io.getstream.chat.android.ui.common.helper.DateFormatter;
import io.getstream.chat.android.ui.feature.messages.list.MessageListView;
import io.getstream.chat.android.ui.feature.messages.list.adapter.BaseMessageItemViewHolder;
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemPayloadDiff;
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewHolderFactory;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelBinding;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelFactory;
import io.getstream.chat.docs.databinding.TodayMessageListItemBinding;

/**
 * [Message List](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list/)
 */
public class MessageList extends Fragment {

    private MessageListView messageListView;

    public void usage() {
        // Init ViewModel
        ViewModelProvider.Factory factory = new MessageListViewModelFactory.Builder()
                .cid("messaging:123")
                .build();
        ViewModelProvider provider = new ViewModelProvider(this, factory);
        MessageListViewModel viewModel = provider.get(MessageListViewModel.class);

        // Bind View and ViewModel
        MessageListViewModelBinding.bind(viewModel, messageListView, getViewLifecycleOwner());
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
        messageListView.setGiphySendHandler((giphyAction) -> {
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
            // Handle message being clicked
        });
        messageListView.setEnterThreadListener((message) -> {
            // Handle thread being entered
        });
        messageListView.setAttachmentDownloadClickListener((attachment) -> {
            // Handle clicks on the download attachment button
        });
        messageListView.setUserReactionClickListener((message, user, reaction) -> {
            // Handle clicks on a reaction left by a user
        });
        messageListView.setMessageLongClickListener((message) -> {
            // Handle message being long clicked
        });
        messageListView.setAttachmentClickListener((message, attachment) -> {
            // Handle attachment being clicked
        });
        messageListView.setUserClickListener((user) -> {
            // Handle user avatar being clicked
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

    @SuppressWarnings({"Lambda can be replaced with method reference", "Convert2MethodRef"})
    public void avatarPredicate() {
        messageListView.setShowAvatarPredicate((messageItem) ->
                messageItem.isTheirs()
        );
    }
}
