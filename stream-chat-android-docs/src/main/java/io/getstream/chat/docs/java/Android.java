package io.getstream.chat.docs.java;

import android.view.ViewGroup;

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
import io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView;
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel;
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModelBinding;
import io.getstream.chat.android.ui.gallery.AttachmentGalleryDestination;
import io.getstream.chat.android.ui.gallery.AttachmentGalleryItem;
import io.getstream.chat.android.ui.message.list.MessageListView;
import io.getstream.chat.android.ui.message.list.adapter.BaseMessageItemViewHolder;
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewHolderFactory;
import io.getstream.chat.android.ui.message.list.header.MessageListHeaderView;
import io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModel;
import io.getstream.chat.android.ui.message.input.MessageInputViewModelBinding;
import io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModelBinding;
import io.getstream.chat.android.ui.message.view.MessageListViewModelBinding;
import io.getstream.chat.android.ui.search.SearchInputView;
import io.getstream.chat.android.ui.search.list.SearchResultListView;
import io.getstream.chat.android.ui.search.list.viewmodel.SearchViewModel;
import io.getstream.chat.android.ui.message.input.MessageInputView;
import io.getstream.chat.android.ui.search.list.viewmodel.SearchViewModelBinding;

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
     * @see <a href="https://getstream.io/chat/docs/android/message_input_view_neo">Message Input View</a>
     */
    class MessageInput extends Fragment {
        MessageInputView messageInputView;

        public void bindingWithViewModel() {
            // Get ViewModel
            MessageInputViewModel viewModel =
                    new ViewModelProvider(this).get(MessageInputViewModel.class);
            // Bind it with MessageInputView
            MessageInputViewModelBinding
                    .bind(viewModel, messageInputView, getViewLifecycleOwner());
        }
    }

    /**
     * * @see <a href="https://getstream.io/chat/docs/android/messages_header_view">Message List Header View</a>
     */
    class MessageListHeader extends Fragment {
        MessageListHeaderView messageListHeaderView;

        public void bindingWithViewModel() {
            // Get ViewModel
            MessageListHeaderViewModel viewModel =
                    new ViewModelProvider(this).get(MessageListHeaderViewModel.class);
            // Bind it with MessagesHeaderView
            MessageListHeaderViewModelBinding
                    .bind(viewModel, messageListHeaderView, getViewLifecycleOwner());
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

        public void bindWithViewModel() {
            MessageListViewModel viewModel =
                    new ViewModelProvider(this).get(MessageListViewModel.class);

            MessageListViewModelBinding.bind(viewModel, messageListView, getViewLifecycleOwner());
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
