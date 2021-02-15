package io.getstream.chat.docs.java;

import android.util.Log;
import android.view.ViewGroup;

import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.getstream.sdk.chat.ChatUI;
import com.getstream.sdk.chat.adapter.MessageListItem;
import com.getstream.sdk.chat.utils.DateFormatter;

import com.getstream.sdk.chat.view.messages.MessageListItemWrapper;
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel;
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.Filters;
import io.getstream.chat.android.client.utils.FilterObject;
import io.getstream.chat.android.livedata.ChatDomain;
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
import io.getstream.chat.android.ui.gallery.AttachmentGalleryDestination;
import io.getstream.chat.android.ui.gallery.AttachmentGalleryItem;
import io.getstream.chat.android.ui.message.input.viewmodel.MessageInputViewModelBinding;
import io.getstream.chat.android.ui.message.list.MessageListView;
import io.getstream.chat.android.ui.message.list.adapter.BaseMessageItemViewHolder;
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewHolderFactory;
import io.getstream.chat.android.ui.message.input.MessageInputView;
import io.getstream.chat.android.ui.message.list.header.MessageListHeaderView;
import io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModel;
import io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModelBinding;
import io.getstream.chat.android.ui.message.list.viewmodel.MessageListViewModelBinding;
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory;
import io.getstream.chat.android.ui.search.SearchInputView;
import io.getstream.chat.android.ui.search.list.SearchResultListView;
import io.getstream.chat.android.ui.search.list.viewmodel.SearchViewModel;
import io.getstream.chat.android.ui.search.list.viewmodel.SearchViewModelBinding;
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

            ChatUI.instance().getNavigator().navigate(destination);
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
}
