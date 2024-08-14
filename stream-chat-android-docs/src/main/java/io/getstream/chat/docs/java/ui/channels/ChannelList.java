package io.getstream.chat.docs.java.ui.channels;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Collections;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.models.Channel;
import io.getstream.chat.android.models.FilterObject;
import io.getstream.chat.android.models.Filters;
import io.getstream.chat.android.models.querysort.QuerySortByField;
import io.getstream.chat.android.ui.ChatUI;
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView;
import io.getstream.chat.android.ui.feature.channels.list.adapter.ChannelListItem;
import io.getstream.chat.android.ui.feature.channels.list.adapter.ChannelListPayloadDiff;
import io.getstream.chat.android.ui.feature.channels.list.adapter.viewholder.BaseChannelListItemViewHolder;
import io.getstream.chat.android.ui.feature.channels.list.adapter.viewholder.ChannelListItemViewHolderFactory;
import io.getstream.chat.android.ui.helper.TransformStyle;
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModel;
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModelBinding;
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModelFactory;
import io.getstream.chat.docs.R;
import io.getstream.chat.docs.databinding.CustomChannelListItemBinding;

/**
 * [Channel List](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list/)
 */
public class ChannelList extends Fragment {

    private ChannelListView channelListView;

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list/#usage)
     */
    public void usage() {
        // Instantiate the ViewModel
        FilterObject filter = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.in("members", Collections.singletonList(ChatClient.instance().getCurrentUser().getId()))
        );

        ViewModelProvider.Factory factory = new ChannelListViewModelFactory.Builder()
                .filter(filter)
                .sort(QuerySortByField.descByName("last_updated"))
                .limit(30)
                .build();

        ChannelListViewModel viewModel = new ViewModelProvider(this, factory).get(ChannelListViewModel.class);

        // Bind the ViewModel with ChannelListView
        ChannelListViewModelBinding.bind(viewModel, channelListView, getViewLifecycleOwner());
    }

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list/#handling-actions)
     */
    public void handlingActions() {
        channelListView.setChannelItemClickListener(channel -> {
            // Handle channel click
        });
        channelListView.setChannelInfoClickListener(channel -> {
            // Handle channel info click
        });
        channelListView.setUserClickListener(user -> {
            // Handle member click
        });
    }

    /**
     * [Customization](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list/#customization)
     */
    public void customization() {
        TransformStyle.setChannelListStyleTransformer(source -> {
            // Customize the style
            return source;
        });
    }

    /**
     * [Creating a Custom ViewHolder Factory](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list/#creating-a-custom-viewholder-factory)
     */
    class CustomViewHolderFactory {

        public class CustomChannelListItemViewHolderFactory extends ChannelListItemViewHolderFactory {
            @NonNull
            @Override
            protected BaseChannelListItemViewHolder createChannelViewHolder(@NonNull ViewGroup parent) {
                CustomChannelListItemBinding binding = CustomChannelListItemBinding
                        .inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new CustomChannelViewHolder(binding, getListenerContainer().getChannelClickListener());
            }
        }

        public class CustomChannelViewHolder extends BaseChannelListItemViewHolder {

            private CustomChannelListItemBinding binding;

            private Channel channel;

            public CustomChannelViewHolder(CustomChannelListItemBinding binding,
                                           ChannelListView.ChannelClickListener channelClickListener) {
                super(binding.getRoot());
                this.binding = binding;

                binding.getRoot().setOnClickListener(v -> channelClickListener.onClick(channel));
            }

            @Override
            public void bind(@NonNull ChannelListItem.ChannelItem channelItem, @NonNull ChannelListPayloadDiff diff) {
                this.channel = channelItem.getChannel();
                binding.channelAvatarView.setChannel(channel);
                String channelName = ChatUI.getChannelNameFormatter().formatChannelName(
                        channel,
                        ChatClient.instance().getCurrentUser()
                );
                binding.channelNameTextView.setText(channelName);
                String memberCount = itemView.getContext().getResources().getQuantityString(
                        R.plurals.members_count,
                        channel.getMembers().size(),
                        channel.getMembers().size()
                );
                binding.membersCountTextView.setText(memberCount);
            }
        }

        public void settingCustomViewHolderFactory() {
            // Create custom view holder factory
            CustomChannelListItemViewHolderFactory customFactory = new CustomChannelListItemViewHolderFactory();

            // Set custom view holder factory
            channelListView.setViewHolderFactory(customFactory);
        }
    }

    /**
     * [Creating a Custom Loading View](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list/#creating-a-custom-loading-view)
     */
    public void customizingLoadingView() {
        // Inflate loading view
        View loadingView = LayoutInflater.from(getContext()).inflate(R.layout.channel_list_loading_view, null);
        // Set loading view
        channelListView.setLoadingView(loadingView, new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }
}
