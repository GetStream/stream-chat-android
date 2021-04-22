package io.getstream.chat.docs.java;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.getstream.sdk.chat.adapter.MessageListItem;
import com.getstream.sdk.chat.utils.DateFormatter;
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper;
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel;
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel;

import io.getstream.chat.android.client.api.models.QueryChannelRequest;
import io.getstream.chat.android.ui.message.input.attachment.internal.AttachmentDialogStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.FilterObject;
import io.getstream.chat.android.client.api.models.QuerySort;
import io.getstream.chat.android.client.errors.ChatError;
import io.getstream.chat.android.client.events.ChatEvent;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.ChannelUserRead;
import io.getstream.chat.android.client.models.Filters;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.livedata.ChatDomain;
import io.getstream.chat.android.livedata.controller.ChannelController;
import io.getstream.chat.android.livedata.controller.QueryChannelsController;
import io.getstream.chat.android.livedata.controller.ThreadController;
import io.getstream.chat.android.offline.utils.RetryPolicy;
import io.getstream.chat.android.ui.TransformStyle;
import io.getstream.chat.android.ui.channel.list.ChannelListView;
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItem;
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelListItemViewHolder;
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelListItemViewHolderFactory;
import io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView;
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel;
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModelBinding;
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel;
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModelBinding;
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory;
import io.getstream.chat.android.ui.common.style.TextStyle;
import io.getstream.chat.android.ui.gallery.AttachmentGalleryDestination;
import io.getstream.chat.android.ui.gallery.AttachmentGalleryItem;
import io.getstream.chat.android.ui.message.input.MessageInputView;
import io.getstream.chat.android.ui.message.input.MessageInputViewStyle;
import io.getstream.chat.android.ui.message.input.viewmodel.MessageInputViewModelBinding;
import io.getstream.chat.android.ui.message.list.MessageListView;
import io.getstream.chat.android.ui.message.list.adapter.BaseMessageItemViewHolder;
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewHolderFactory;
import io.getstream.chat.android.ui.message.list.header.MessageListHeaderView;
import io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModel;
import io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModelBinding;
import io.getstream.chat.android.ui.message.list.viewmodel.MessageListViewModelBinding;
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory;
import io.getstream.chat.android.ui.search.SearchInputView;
import io.getstream.chat.android.ui.search.list.SearchResultListView;
import io.getstream.chat.android.ui.search.list.viewmodel.SearchViewModel;
import io.getstream.chat.android.ui.search.list.viewmodel.SearchViewModelBinding;
import io.getstream.chat.android.ui.suggestion.list.SuggestionListView;
import io.getstream.chat.docs.R;

import static java.util.Collections.singletonList;

public class Android {

    /**
     * @see <a href="https://getstream.io/chat/docs/android/channel_list_header_view">Channel List Header View</a>
     */
    class ChannelListHeader extends Fragment {
        ChannelListHeaderView channelListHeaderView;

        public void bindingWithViewModel() {
            // Get ViewModel
            ChannelListHeaderViewModel viewModel = new ViewModelProvider(this).get(ChannelListHeaderViewModel.class);
            // Bind it with ChannelListHeaderView
            ChannelListHeaderViewModelBinding.bind(viewModel, channelListHeaderView, getViewLifecycleOwner());
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/node/channels_view_new/">Channel List View</a>
     */
    class ChannelList extends Fragment {

        ChannelListView channelListView;

        public void bindingWithViewModel() {
            // Get ViewModel
            FilterObject filter = Filters.and(
                    Filters.eq("type", "messaging"),
                    Filters.in("members", singletonList(ChatDomain.instance().getCurrentUser().getId()))
            );
            int limit = 30;

            ChannelListViewModelFactory factory = new ChannelListViewModelFactory(filter, ChannelListViewModel.DEFAULT_SORT, limit);
            ChannelListViewModel viewModel = new ViewModelProvider(this, factory)
                    .get(ChannelListViewModel.class);

            // Bind it with ChannelListView
            ChannelListViewModelBinding.bind(viewModel, channelListView, getViewLifecycleOwner());
        }

        public void handlingChannelActions() {
            channelListView.setChannelInfoClickListener((channel) -> {
                // Handle Channel Info Click
            });

            channelListView.setUserClickListener((user) -> {
                // Handle Member Click
            });
        }

        public void handlingUserInteractions() {
            channelListView.setChannelItemClickListener((channel) -> {
                // Handle Channel Click
            });

            channelListView.setChannelLongClickListener((channel) -> {
                // Handle Channel Long Click
                return true;
            });
        }

        public void customizingDefaultViews() {
            // Create loading view and layout params
            ProgressBar loadingView = new ProgressBar(getContext());
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
            );
            channelListView.setEmptyStateView(loadingView, layoutParams);

            // Create empty state view and use default layout params
            TextView emptyStateView = new TextView(getContext());
            emptyStateView.setText("No channels available");
            channelListView.setEmptyStateView(emptyStateView);


            // Set custom item separator drawable
            channelListView.setItemSeparator(R.drawable.stream_ui_divider);

            // Add separator to the last item
            channelListView.setShouldDrawItemSeparatorOnLastItem(true);
        }

        public void customViewHolderFactory() {
            class CustomChannelListItemViewHolderFactory extends ChannelListItemViewHolderFactory {
                @Override
                public int getItemViewType(@NotNull ChannelListItem item) {
                    // Override together with createViewHolder() to introduce different view holder types
                    return super.getItemViewType(item);
                }

                @NotNull
                @Override
                public BaseChannelListItemViewHolder createViewHolder(@NotNull ViewGroup parentView, int viewType) {
                    // Override to create custom create view holder logic
                    return super.createViewHolder(parentView, viewType);
                }

                @NotNull
                @Override
                protected BaseChannelListItemViewHolder createChannelViewHolder(@NotNull ViewGroup parentView) {
                    // Create custom channel view holder
                    return super.createChannelViewHolder(parentView);
                }

                @NotNull
                @Override
                protected BaseChannelListItemViewHolder createLoadingMoreViewHolder(@NotNull ViewGroup parentView) {
                    // Create custom loading more view holder
                    return super.createLoadingMoreViewHolder(parentView);
                }
            }

            // Create custom view holder factory
            CustomChannelListItemViewHolderFactory customFactory = new CustomChannelListItemViewHolderFactory();

            // Set custom view holder factory
            channelListView.setViewHolderFactory(customFactory);
        }

        public void otherCustomizations() {
            TransformStyle.INSTANCE.setChannelListStyleTransformer(viewStyle -> {
                        // Modify default view style
                        return viewStyle;
                    }
            );
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/message_input_view_neo">Message Input View</a>
     */
    class MessageInput extends Fragment {
        MessageInputView messageInputView;

        public void bindingWithViewModel() {
            // Get ViewModel
            MessageListViewModelFactory factory = new MessageListViewModelFactory("channelType:channelId");
            MessageInputViewModel viewModel =
                    new ViewModelProvider(this, factory).get(MessageInputViewModel.class);
            // Bind it with MessageInputView
            MessageInputViewModelBinding
                    .bind(viewModel, messageInputView, getViewLifecycleOwner());
        }

        public void handlingUserInteractions() {
            messageInputView.setOnSendButtonClickListener(() -> {
                // Handle send button click
            });
            messageInputView.setTypingListener(new MessageInputView.TypingListener() {
                @Override
                public void onKeystroke() {
                    // Handle send button click
                }

                @Override
                public void onStopTyping() {
                    // Handle stop typing case
                }
            });
        }
    }

    /**
     * * @see <a href="https://getstream.io/chat/docs/android/message_list_header_view">Message List Header View</a>
     */
    class MessageListHeader extends Fragment {
        MessageListHeaderView messageListHeaderView;

        public void bindingWithViewModel() {
            // Get ViewModel
            MessageListViewModelFactory factory = new MessageListViewModelFactory("channelType:channelId");
            MessageListHeaderViewModel viewModel =
                    new ViewModelProvider(this, factory).get(MessageListHeaderViewModel.class);
            // Bind it with MessageListHeaderView
            MessageListHeaderViewModelBinding
                    .bind(viewModel, messageListHeaderView, getViewLifecycleOwner());
        }

        public void handlingUserInteractions() {
            messageListHeaderView.setAvatarClickListener(() -> {
                // Handle avatar click
            });
            messageListHeaderView.setTitleClickListener(() -> {
                // Handle title click
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/nessy/docs/chat_docs/android_chat_ux/search_input_view">Search Input View</a>
     */
    class SearchInput extends Fragment {
        SearchInputView searchInputView;

        public void listeningForSearchQueryChanges() {
            searchInputView.setContinuousInputChangedListener(query -> {
                // Search query changed
            });
            searchInputView.setDebouncedInputChangedListener(query -> {
                // Search query changed and has been stable for a short while
            });
            searchInputView.setSearchStartedListener(query -> {
                // Search is triggered
            });

            // Update the current search query programmatically
            searchInputView.setQuery("query");
            // Clear the current search query programmatically
            searchInputView.clear();
        }
    }

    /**
     * @see <a href="https://getstream.io/nessy/docs/chat_docs/android_chat_ux/search_result_list_view">Search Result List View</a>
     */
    class SearchResultList extends Fragment {
        SearchInputView searchInputView;
        SearchResultListView searchResultListView;

        public void bindingWithViewModel() {
            // Get ViewModel
            SearchViewModel viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
            // Bind it with SearchResultListView
            SearchViewModelBinding.bind(viewModel, searchResultListView, getViewLifecycleOwner());
            // Notify ViewModel when search is triggered
            searchInputView.setSearchStartedListener(query -> {
                viewModel.setQuery(query);
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/nessy/docs/chat_docs/android_chat_ux/attachmentgallery">Attachment Gallery Activity</a>
     */
    class AttachmentGalleryActivity extends Fragment {
        public void navigateTo() {
            AttachmentGalleryDestination destination = new AttachmentGalleryDestination(
                    getContext(),
                    resultItem -> {
                        // Handle reply
                    },
                    resultItem -> {
                        // Handle show in chat
                    },
                    resultItem -> {
                        // Handle download image
                    },
                    resultItem -> {
                        // Handle delete image
                    });

            List<AttachmentGalleryItem> attachmentGalleryItems = new ArrayList();

            destination.register(getActivity().getActivityResultRegistry());
            destination.setData(attachmentGalleryItems, 0);
        }
    }

    /**
     * @see <a href="https://getstream.io/nessy/docs/chat_docs/android_chat_ux/message_list_view_new">Message List View</a>
     */
    class MessageListViewDocs extends Fragment {
        MessageListView messageListView;
        private final MessageListViewModel viewModel =
                new ViewModelProvider(this).get(MessageListViewModel.class);

        public void emptyState() {
            messageListView.showEmptyStateView();
        }

        public void loadingView() {
            //When loading information, show loading view
            messageListView.showLoadingView();
        }

        public void viewHolderFactory() {
            MessageListItemViewHolderFactory factory =
                    new MessageListItemViewHolderFactoryExtended();
            messageListView.setMessageViewHolderFactory(factory);
        }

        public void messageClick() {
            messageListView.setMessageClickListener(message -> {
                // Handle message click
            });
        }

        public void messageLongClick() {
            messageListView.setMessageLongClickListener(message -> {
                // Handle message long click
            });
        }

        public void dateFormatter() {
            messageListView.setMessageDateFormatter(new DateFormatter() {
                @NotNull
                @Override
                public String formatDate(@Nullable LocalDateTime localDateTime) {
                    return DateTimeFormatter.ofPattern("dd/MM/yyyy").format(localDateTime);
                }

                @NotNull
                @Override
                public String formatTime(@Nullable LocalTime localTime) {
                    return DateTimeFormatter.ofPattern("HH:mm").format(localTime);
                }
            });
        }

        public void customMessagesFilter() {
            messageListView.setMessageListItemPredicate(messageList -> {
                // Boolean logic here
                return true;
            });
        }

        public void usingTransformStyle() {
            TransformStyle.INSTANCE.setMessageListStyleTransformer(defaultMessageListViewStyle -> {
                // Modify default MessageListView style
                return defaultMessageListViewStyle;
            });

            TransformStyle.INSTANCE.setMessageListItemStyleTransformer(defaultMessageListItemStyle -> {
                // Modify default MessageListItem style
                return defaultMessageListItemStyle;
            });
        }

        public void setNewMessageBehaviour() {
            messageListView.setNewMessagesBehaviour(
                    MessageListView.NewMessagesBehaviour.COUNT_UPDATE
            );
        }

        public void setEndRegionReachedHandler() {
            messageListView.setEndRegionReachedHandler(() -> {
                // Handle pagination and include new logic

                // Option to log the event and use the viewModel
                viewModel.onEvent(MessageListViewModel.Event.EndRegionReached.INSTANCE);
                Log.e("LogTag", "On load more");
            });
        }

        public void bindWithViewModel() {
            // Get ViewModel
            MessageListViewModelFactory factory = new MessageListViewModelFactory("channelType:channelId");
            MessageListViewModel viewModel =
                    new ViewModelProvider(this, factory).get(MessageListViewModel.class);

            // Bind it with MessageListView
            MessageListViewModelBinding.bind(viewModel, messageListView, getViewLifecycleOwner());
        }

        public void handlingUserInteractions() {
            messageListView.setMessageClickListener((message) -> {
                // Handle click on message
            });
            messageListView.setMessageLongClickListener((message) -> {
                // Handle long click on message
            });
            messageListView.setAttachmentClickListener((message, attachment) -> {
                // Handle click on attachment
            });
            messageListView.setUserClickListener((user) -> {
                // Handle click on user avatar
            });
        }

        public void handlers() {
            messageListView.setMessageEditHandler((message) -> {
                // Handle edit message
            });
            messageListView.setMessageDeleteHandler((message) -> {
                // Handle delete message
            });
            messageListView.setAttachmentDownloadHandler((attachment) -> {
                // Handle attachment download
            });
        }

        public void displayNewMessage() {
            Message message = new Message();
            message.setText("Lorem ipsum dolor");
            MessageListItem.MessageItem messageItem = new MessageListItem.MessageItem(
                    message, new ArrayList<>(), true, new ArrayList<>(), false, false
            );

            MessageListItemWrapper messageWrapper = new MessageListItemWrapper(
                    Collections.singletonList(messageItem), false, false, false
            );

            messageListView.displayNewMessages(messageWrapper);
        }

        class MessageListItemViewHolderFactoryExtended extends MessageListItemViewHolderFactory {
            @NotNull
            @Override
            public BaseMessageItemViewHolder<? extends MessageListItem> createViewHolder(@NotNull ViewGroup parentView, int viewType) {
                // Create a new type of view holder here, if needed
                return super.createViewHolder(parentView, viewType);
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/nessy/docs/chat_docs/android_chat_ux/combining_view_models">Combining Views and View Models</a>
     */
    class CombiningViewsAndViewModels extends Fragment {
        MessageListView messageListView;
        MessageListHeaderView messageListHeaderView;
        MessageListViewModel messageListViewModel;
        MessageListHeaderViewModel messageListHeaderViewModel;
        MessageInputViewModel messageInputViewModel;

        public void handlingThreads() {
            messageListViewModel.getMode().observe(getViewLifecycleOwner(), (mode) -> {
                if (mode instanceof MessageListViewModel.Mode.Thread) {
                    // Handle entering thread mode
                    Message parentMessage = ((MessageListViewModel.Mode.Thread) mode).getParentMessage();
                    messageListHeaderViewModel.setActiveThread(parentMessage);
                    messageInputViewModel.setActiveThread(parentMessage);
                } else if (mode instanceof MessageListViewModel.Mode.Normal) {
                    // Handle leaving thread mode
                    messageListHeaderViewModel.resetThread();
                    messageInputViewModel.resetThread();
                }
            });
        }

        public void editingMessage() {
            messageListView.setMessageEditHandler((message) -> {
                messageInputViewModel.getEditMessage().postValue(message);
            });
        }

        public void handlingBackButtonClicks() {
            messageListHeaderView.setBackButtonClickListener(() -> {
                messageListViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed.INSTANCE);
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/nessy/docs/chat_docs/android_chat_ux/suggestion_list_view">Suggestion List View</a>
     */
    public class SuggestionList extends Fragment {
        MessageInputView messageInputView;
        SuggestionListView suggestionListView;

        public void connectingSuggestionListViewWithMessageInputView() {
            messageInputView.setSuggestionListView(suggestionListView);
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/android_offline/?language=java">Android Offline</a>
     */
    public class AndroidOffline extends Fragment {

        public void initializeChatDomain() {
            ChatClient chatClient =
                    new ChatClient.Builder("apiKey", requireContext()).build();
            ChatDomain chatDomain = new ChatDomain.Builder(chatClient, requireContext())
                    .offlineEnabled()
                    .userPresenceEnabled()
                    .build();
        }

        public void getChatDomainInstance() {
            ChatDomain chatDomain = ChatDomain.instance();

            ChatClient chatClient = ChatClient.instance();
            chatClient.disconnect();
        }

        public void customizeRetryPolicy() {
            ChatDomain chatDomain = ChatDomain.instance();

            chatDomain.setRetryPolicy(new RetryPolicy() {
                @Override
                public boolean shouldRetry(@NotNull ChatClient client, int attempt, @NotNull ChatError error) {
                    return attempt < 3;
                }

                @Override
                public int retryTimeout(@NotNull ChatClient client, int attempt, @NotNull ChatError error) {
                    return 1000 * attempt;
                }
            });
        }

        public void watchChannel() {
            ChatDomain chatDomain = ChatDomain.instance();

            chatDomain.getUseCases().getWatchChannel().invoke("messaging:123", 0)
                    .enqueue(result -> {
                        if (result.isSuccess()) {
                            ChannelController channelController = result.data();

                            // LiveData objects to observe
                            channelController.getMessages();
                            channelController.getReads();
                            channelController.getTyping();
                        }
                    });
        }

        public void loadMoreMessages() {
            ChatDomain chatDomain = ChatDomain.instance();

            chatDomain.getUseCases().getLoadOlderMessages().invoke("messaging:123", 10)
                    .enqueue(result -> {
                        if (result.isSuccess()) {
                            Channel channel = result.data();
                        }
                    });
        }

        public void sendMessage() {
            ChatDomain chatDomain = ChatDomain.instance();
            Message message = new Message();
            message.setText("Hello world");

            chatDomain.getUseCases().getSendMessage().invoke(message)
                    .enqueue(result -> {
                        if (result.isSuccess()) {
                            Message message1 = result.data();
                        }
                    });
        }

        public void queryChannels() {
            ChatDomain chatDomain = ChatDomain.instance();

            List<String> members = new ArrayList<>();
            members.add("thierry");

            FilterObject filter = Filters.and(
                    Filters.eq("type", "messaging"),
                    Filters.in("members", members)
            );
            QuerySort<Channel> sort = new QuerySort<>();

            int limit = 10;
            int messageLimit = 1;

            chatDomain.getUseCases().getQueryChannels().invoke(filter, sort, limit, messageLimit)
                    .enqueue(result -> {
                        if (result.isSuccess()) {
                            final QueryChannelsController controller = result.data();

                            // LiveData objects to observe
                            controller.getChannels();
                            controller.getLoading();
                            controller.getEndOfChannels();
                        }
                    });
        }

        public void loadMoreFromChannel() {
            ChatDomain chatDomain = ChatDomain.instance();

            List<String> members = new ArrayList<>();
            members.add("thierry");

            FilterObject filter = Filters.and(
                    Filters.eq("type", "messaging"),
                    Filters.in("members", members)
            );
            QuerySort<Channel> sort = new QuerySort<>();
            int limit = 10;
            int messageLimit = 1;

            chatDomain.getUseCases().getQueryChannelsLoadMore().invoke(filter, sort, limit, messageLimit)
                    .enqueue(result -> {
                        if (result.isSuccess()) {
                            final List<Channel> channels = result.data();
                        }
                    });
        }

        public void unreadCount() {
            ChatDomain chatDomain = ChatDomain.instance();

            // LiveData objects to observe
            LiveData<Integer> totalUnreadCount = chatDomain.getUseCases()
                    .getGetTotalUnreadCount()
                    .invoke()
                    .execute()
                    .data();
            LiveData<Integer> unreadChannelCount = chatDomain.getUseCases()
                    .getGetUnreadChannelCount()
                    .invoke()
                    .execute().data();
        }

        public void messagesFromThread() {
            ChatDomain chatDomain = ChatDomain.instance();

            chatDomain.getUseCases().getGetThread().invoke("cid", "parentId")
                    .enqueue(result -> {
                        if (result.isSuccess()) {
                            final ThreadController threadController = result.data();

                            // LiveData objects to observe
                            threadController.getMessages();
                            threadController.getLoadingOlderMessages();
                            threadController.getEndOfOlderMessages();
                        }
                    });
        }

        public void loadMoreFromThread() {
            ChatDomain chatDomain = ChatDomain.instance();
            int messageLimit = 1;

            chatDomain.getUseCases().getThreadLoadMore().invoke("cid", "parentId", messageLimit)
                    .enqueue(result -> {
                        if (result.isSuccess()) {
                            final List<Message> messages = result.data();
                        }
                    });
        }
    }

    /**
     * @see <a href="hhttps://getstream.io/nessy/docs/chat_docs/events/event_listening/?language=java">Listening for events</a>
     */
    public class SyncHistory extends Fragment {

        public void getSyncHistory(ChatClient chatClient) {
            List<String> cidList = new ArrayList<>();
            cidList.add("messaging:123");

            Date lastSeenExample = new Date();

            chatClient.getSyncHistory(cidList, lastSeenExample).enqueue(result -> {
                if (result.isSuccess()) {
                    List<ChatEvent> events = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/unread_channel/?language=java">Channels</a>
     */
    public class UnreadCount extends Fragment {

        public void unreadCountInfo() {
            // Get channel
            QueryChannelRequest queryChannelRequest = new QueryChannelRequest();

            Channel channel = ChatClient.instance().queryChannel(
                    "channel-type",
                    "channel-id",
                    queryChannelRequest
            )
                    .execute()
                    .data();

            // readState is the list of read states for each user on the channel
            List<ChannelUserRead> readState = channel.getRead();
        }

        public void getUnreadCountInfoChatDomain() {
            // Get channel
            Channel channel = ChatDomain.instance()
                    .watchChannel("messaging:123", 0)
                    .execute()
                    .data()
                    .toChannel();

            // readState is the list of read states for each user on the channel
            List<ChannelUserRead> userReadList = channel.getRead();
        }

        public void getUnreadCountForCurrentUser() {
            // Get channel
            QueryChannelRequest queryChannelRequest = new QueryChannelRequest();

            Channel channel = ChatClient.instance().queryChannel(
                    "channel-type",
                    "channel-id",
                    queryChannelRequest
            )
                    .execute()
                    .data();

            // Unread count for current user
            int unreadCount = channel.getUnreadCount();
        }

        public void getUnreadCountForCurrentUserChatDomain() {
            // Get channel controller
            ChannelController channelController = ChatDomain.instance()
                    .watchChannel("messaging:123", 0)
                    .execute()
                    .data();

            //Unread count for current user
            LiveData<Integer> unreadCount = channelController.getUnreadCount();
        }

        public void markAllRead() {
            ChatClient.instance().markAllRead().enqueue(result -> {
                if (result.isSuccess()) {
                    //Handle success
                } else {
                    //Handle failure
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/nessy/docs/chat_docs/android_chat_ux/message_input_view?language=java">Message Input View</a>
     */
    public class TransformStyleMessageInput extends Fragment {
        public void messageInputCustomisation() {
            TextStyle textStyleGeneric = new TextStyle(
                    0,
                    "fontAsserts",
                    Typeface.NORMAL,
                    requireContext().getResources().getDimensionPixelSize(R.dimen.stream_ui_text_medium),
                    ContextCompat.getColor(getContext(), R.color.stream_ui_black),
                    "some hint",
                    ContextCompat.getColor(getContext(), R.color.stream_ui_black),
                    Typeface.DEFAULT
            );

            int colorBlack = ContextCompat.getColor(getContext(), R.color.stream_ui_black);

            Drawable genericDrawable =
                    ContextCompat.getDrawable(getContext(), R.drawable.stream_ui_ic_command);

            AttachmentDialogStyle attachmentDialogStyle = new AttachmentDialogStyle(
                    genericDrawable,
                    ColorStateList.valueOf(colorBlack),
                    genericDrawable,
                    ColorStateList.valueOf(colorBlack),
                    genericDrawable,
                    ColorStateList.valueOf(colorBlack)
            );

            TransformStyle.INSTANCE.setMessageInputStyleTransformer(
                    viewStyle ->
                        new MessageInputViewStyle(
                                true,
                                genericDrawable,
                                true,
                                genericDrawable,
                                requireContext().getResources().getDimension(R.dimen.stream_ui_text_medium),
                                colorBlack,
                                colorBlack,
                                textStyleGeneric,
                                true,
                                true,
                                true,
                                genericDrawable,
                                genericDrawable,
                                true,
                                true,
                                textStyleGeneric,
                                textStyleGeneric,
                                textStyleGeneric,
                                true,
                                textStyleGeneric,
                                textStyleGeneric,
                                genericDrawable,
                                colorBlack,
                                colorBlack,
                                genericDrawable,
                                genericDrawable,
                                20,
                                genericDrawable,
                                attachmentDialogStyle
                        )

            );
        }
    }
}
