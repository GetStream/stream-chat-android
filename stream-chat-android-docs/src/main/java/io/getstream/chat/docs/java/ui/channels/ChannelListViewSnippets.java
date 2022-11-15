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
import io.getstream.chat.android.models.FilterObject;
import io.getstream.chat.android.models.Filters;
import io.getstream.chat.android.ui.helper.TransformStyle;
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView;
import io.getstream.chat.android.ui.feature.channels.list.adapter.ChannelListItem;
import io.getstream.chat.android.ui.feature.channels.list.adapter.viewholder.BaseChannelListItemViewHolder;
import io.getstream.chat.android.ui.feature.channels.list.adapter.viewholder.ChannelListItemViewHolderFactory;
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModel;
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModelFactory;
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModelBinding;
import io.getstream.chat.docs.R;

/**
 * [Channel List](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list/)
 */
public class ChannelListViewSnippets extends Fragment {

    private ChannelListView channelListView;

    public void usage() {
        // Instantiate the ViewModel
        FilterObject filter = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.in("members", Collections.singletonList(ChatClient.instance().getCurrentUser().getId()))
        );

        ViewModelProvider.Factory factory = new ChannelListViewModelFactory.Builder()
                .filter(filter)
                .sort(ChannelListViewModel.DEFAULT_SORT)
                .limit(30)
                .build();

        ChannelListViewModel viewModel = new ViewModelProvider(this, factory).get(ChannelListViewModel.class);

        // Bind the ViewModel with ChannelListView
        ChannelListViewModelBinding.bind(viewModel, channelListView, getViewLifecycleOwner());
    }

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

    public void customization() {
        TransformStyle.setChannelListStyleTransformer(source -> {
            // Customize the style
            return source;
        });
    }

    public final class CustomChannelListItemViewHolderFactory extends ChannelListItemViewHolderFactory {
        @Override
        public int getItemViewType(@NonNull ChannelListItem item) {
            // Override together with createViewHolder() to introduce different view holder types
            return super.getItemViewType(item);
        }

        @NonNull
        @Override
        public BaseChannelListItemViewHolder createViewHolder(@NonNull ViewGroup parentView, int viewType) {
            // Override to create custom create view holder logic
            return super.createViewHolder(parentView, viewType);
        }

        @NonNull
        @Override
        protected BaseChannelListItemViewHolder createChannelViewHolder(@NonNull ViewGroup parentView) {
            // Create custom channel view holder
            return super.createChannelViewHolder(parentView);
        }

        @NonNull
        @Override
        protected BaseChannelListItemViewHolder createLoadingMoreViewHolder(@NonNull ViewGroup parentView) {
            // Create custom loading more view holder
            return super.createLoadingMoreViewHolder(parentView);
        }
    }

    public void customViewHolderFactory() {
        // Create custom view holder factory
        CustomChannelListItemViewHolderFactory customFactory = new CustomChannelListItemViewHolderFactory();

        // Set custom view holder factory
        channelListView.setViewHolderFactory(customFactory);
    }

    public void customizingLoadingView() {
        // Inflate loading view
        View loadingView = LayoutInflater.from(getContext()).inflate(R.layout.channel_list_loading_view, channelListView);
        // Set loading view
        channelListView.setLoadingView(loadingView, new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }
}
