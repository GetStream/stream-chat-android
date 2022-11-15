package io.getstream.chat.docs.java.ui.channels;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.getstream.chat.android.client.api.models.querysort.QuerySorter;
import io.getstream.chat.android.models.FilterObject;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.ui.feature.channels.ChannelListActivity;
import io.getstream.chat.android.ui.feature.channels.ChannelListFragment;
import io.getstream.chat.android.ui.feature.channels.header.ChannelListHeaderView;
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView;
import io.getstream.chat.android.ui.feature.search.SearchInputView;
import io.getstream.chat.android.ui.feature.search.list.SearchResultListView;
import io.getstream.chat.docs.R;
import kotlin.Unit;

/**
 * [Channel List Screen](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list-screen/)
 */
public class ChannelListScreen {

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list-screen/#usage)
     */
    class Usage {

        public void startingActivity(Context context) {
            context.startActivity(ChannelListActivity.createIntent(context));
        }

        public final class MyChannelListActivity extends AppCompatActivity {

            public MyChannelListActivity() {
                super(R.layout.stream_ui_fragment_container);
            }

            @Override
            protected void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                if (savedInstanceState == null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, ChannelListFragment.newInstance())
                            .commit();
                }
            }
        }
    }

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list-screen/#handling-actions)
     */
    class HandlingActions {

        public final class MyChannelListActivity extends AppCompatActivity implements
                ChannelListFragment.HeaderActionButtonClickListener,
                ChannelListFragment.HeaderUserAvatarClickListener,
                ChannelListFragment.ChannelListItemClickListener,
                ChannelListFragment.SearchResultClickListener {

            public MyChannelListActivity() {
                super(R.layout.stream_ui_fragment_container);
            }

            @Override
            protected void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                if (savedInstanceState == null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, ChannelListFragment.newInstance())
                            .commit();
                }
            }

            @Override
            public void onUserAvatarClick() {
                // Handle header avatar click
            }

            @Override
            public void onActionButtonClick() {
                // Handle header action button click
            }

            @Override
            public void onChannelClick(@NonNull Channel channel) {
                // Handle channel click
            }

            @Override
            public void onSearchResultClick(@NonNull Message message) {
                // Handle search result click
            }
        }
    }

    /**
     * [Customization](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list-screen/#customization)
     */
    class Customization {

        public final class CustomChannelListFragment extends ChannelListFragment {

            @Override
            protected void setupChannelListHeader(@NonNull ChannelListHeaderView channelListHeaderView) {
                super.setupChannelListHeader(channelListHeaderView);
                // Customize channel list header view

                // For example, set a custom listener for the avatar
                channelListHeaderView.setOnUserAvatarClickListener(() -> {
                    // Handle avatar click
                });
            }

            @Override
            protected void setupChannelList(@NonNull ChannelListView channelListView) {
                super.setupChannelList(channelListView);
                // Customize channel list view
            }

            @Override
            protected void setupSearchInput(@NonNull SearchInputView searchInputView) {
                super.setupSearchInput(searchInputView);
                // Customize search input field
            }

            @Override
            protected void setupSearchResultList(@NonNull SearchResultListView searchResultListView) {
                super.setupSearchResultList(searchResultListView);
                // Customize search result list
            }

            @Nullable
            @Override
            protected FilterObject getFilter() {
                // Provide custom filter
                return super.getFilter();
            }

            @NonNull
            @Override
            protected QuerySorter<Channel> getSort() {
                // Provide custom sort
                return super.getSort();
            }
        }

        public final class CustomChannelListActivity extends ChannelListActivity {

            @NonNull
            @Override
            protected ChannelListFragment createChannelListFragment() {
                return ChannelListFragment.newInstance(builder -> {
                    builder.setFragment(new CustomChannelListFragment());
                    builder.customTheme(R.style.StreamUiTheme);
                    builder.showSearch(true);
                    builder.showHeader(true);
                    builder.headerTitle("Title");
                    return Unit.INSTANCE;
                });
            }
        }

        public void startActivity(Context context) {
            context.startActivity(ChannelListActivity.createIntent(context, CustomChannelListActivity.class));
        }
    }
}
