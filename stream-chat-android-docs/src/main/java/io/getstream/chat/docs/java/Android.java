package io.getstream.chat.docs.java;

import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.getstream.sdk.chat.ChatUI;
import com.getstream.sdk.chat.viewmodel.ChannelHeaderViewModel;
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel;
import com.getstream.sdk.chat.viewmodel.channels.ChannelsViewModel;
import com.getstream.sdk.chat.viewmodel.factory.ChannelsViewModelFactory;

import org.jetbrains.annotations.NotNull;

import io.getstream.chat.android.client.models.Filters;
import io.getstream.chat.android.client.utils.FilterObject;
import io.getstream.chat.android.livedata.ChatDomain;
import io.getstream.chat.android.ui.channel.list.ChannelsView;
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItem;
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelListItemViewHolder;
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelListItemViewHolderFactory;
import java.util.ArrayList;
import java.util.List;

import io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView;
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel;
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModelBinding;
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelsViewModelBinding;
import io.getstream.chat.android.ui.gallery.AttachmentGalleryDestination;
import io.getstream.chat.android.ui.gallery.AttachmentGalleryItem;
import io.getstream.chat.android.ui.messages.header.ChannelHeaderViewModelBinding;
import io.getstream.chat.android.ui.messages.header.MessagesHeaderView;
import io.getstream.chat.android.ui.textinput.MessageInputView;
import io.getstream.chat.android.ui.textinput.MessageInputViewModelBinding;
import io.getstream.chat.docs.R;

import static java.util.Collections.singletonList;
import io.getstream.chat.android.ui.search.SearchInputView;
import io.getstream.chat.android.ui.search.SearchResultListView;
import io.getstream.chat.android.ui.search.SearchViewModel;
import io.getstream.chat.android.ui.search.SearchViewModelBinding;

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
     * @see <a href="https://getstream.io/chat/docs/node/channels_view_new/">Channels View</a>
     */
    class Channels extends Fragment {

        ChannelsView channelsView;

        public void bindingWithViewModel() {
            // Get ViewModel
            FilterObject filter = Filters.and(
                    Filters.eq("type", "messaging"),
                    Filters.in("members", singletonList(ChatDomain.instance().getCurrentUser().getId()))
            );
            int limit = 30;

            ChannelsViewModelFactory factory = new ChannelsViewModelFactory(filter, ChannelsViewModel.DEFAULT_SORT, limit);
            ChannelsViewModel viewModel = new ViewModelProvider(this, factory)
                    .get(ChannelsViewModel.class);

            // Bind it with ChannelsView
            ChannelsViewModelBinding.bind(viewModel, channelsView, getViewLifecycleOwner());
        }

        public void handlingChannelActions() {
            channelsView.setChannelInfoClickListener((channel) -> {
                // Handle Channel Info Click
            });

            channelsView.setUserClickListener((user) -> {
                // Handle Member Click
            });
        }

        public void handlingUserInteractions() {
            channelsView.setChannelItemClickListener((channel) -> {
                // Handle Channel Click
            });

            channelsView.setChannelLongClickListener((channel) -> {
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
            channelsView.setEmptyStateView(loadingView, layoutParams);

            // Create empty state view and use default layout params
            TextView emptyStateView = new TextView(getContext());
            emptyStateView.setText("No channels available");
            channelsView.setEmptyStateView(emptyStateView);


            // Set custom item separator drawable
            channelsView.setItemSeparator(R.drawable.stream_ui_divider);

            // Add separator to the last item
            channelsView.setShouldDrawItemSeparatorOnLastItem(true);
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
            channelsView.setViewHolderFactory(customFactory);
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
     * * @see <a href="https://getstream.io/chat/docs/android/messages_header_view">Messages Header View</a>
     */
    class MessagesHeader extends Fragment {
        MessagesHeaderView messagesHeaderView;

        public void bindingWithViewModel() {
            // Get ViewModel
            ChannelHeaderViewModel viewModel =
                    new ViewModelProvider(this).get(ChannelHeaderViewModel.class);
            // Bind it with MessagesHeaderView
            ChannelHeaderViewModelBinding
                    .bind(viewModel, messagesHeaderView, getViewLifecycleOwner());
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
}
