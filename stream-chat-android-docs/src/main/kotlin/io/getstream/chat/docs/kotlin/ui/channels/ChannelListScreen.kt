package io.getstream.chat.docs.kotlin.ui.channels

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.client.api.models.querysort.QuerySorter
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.feature.channels.ChannelListActivity
import io.getstream.chat.android.ui.feature.channels.ChannelListFragment
import io.getstream.chat.android.ui.feature.channels.header.ChannelListHeaderView
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView
import io.getstream.chat.android.ui.feature.search.SearchInputView
import io.getstream.chat.android.ui.feature.search.list.SearchResultListView
import io.getstream.chat.docs.R

/**
 * [Channel List Screen](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list-screen/)
 */
class ChannelListScreen {

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list-screen/#usage)
     */
    fun usage() {

        fun startingActivity(context: Context) {
            context.startActivity(ChannelListActivity.createIntent(context))
        }

        class MyChannelListActivity : AppCompatActivity(R.layout.stream_ui_fragment_container) {

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                if (savedInstanceState == null) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, ChannelListFragment.newInstance())
                        .commit()
                }
            }
        }
    }

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list-screen/#handling-actions)
     */
    fun handlingActions() {

        class MyChannelListActivity : AppCompatActivity(R.layout.stream_ui_fragment_container),
            ChannelListFragment.HeaderActionButtonClickListener,
            ChannelListFragment.HeaderUserAvatarClickListener,
            ChannelListFragment.ChannelListItemClickListener,
            ChannelListFragment.SearchResultClickListener {

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                if (savedInstanceState == null) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, ChannelListFragment.newInstance())
                        .commit()
                }
            }

            override fun onUserAvatarClick() {
                // Handle header avatar click
            }

            override fun onActionButtonClick() {
                // Handle header action button click
            }

            override fun onChannelClick(channel: Channel) {
                // Handle channel click
            }

            override fun onSearchResultClick(message: Message) {
                // Handle search result click
            }
        }
    }

    /**
     * [Customization](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list-screen/#customization)
     */
    fun customization() {

        class CustomChannelListFragment : ChannelListFragment() {

            override fun setupChannelListHeader(channelListHeaderView: ChannelListHeaderView) {
                super.setupChannelListHeader(channelListHeaderView)
                // Customize channel list header view

                // For example, set a custom listener for the avatar
                channelListHeaderView.setOnUserAvatarClickListener {
                    // Handle avatar click
                }
            }

            override fun setupChannelList(channelListView: ChannelListView) {
                super.setupChannelList(channelListView)
                // Customize channel list view
            }

            override fun setupSearchInput(searchInputView: SearchInputView) {
                super.setupSearchInput(searchInputView)
                // Customize search input field
            }

            override fun setupSearchResultList(searchResultListView: SearchResultListView) {
                super.setupSearchResultList(searchResultListView)
                // Customize search result list
            }

            override fun getFilter(): FilterObject? {
                // Provide custom filter
                return null
            }

            override fun getSort(): QuerySorter<Channel> {
                // Provide custom sort
                return super.getSort()
            }
        }

        class CustomChannelListActivity : ChannelListActivity() {

            override fun createChannelListFragment(): ChannelListFragment {
                return ChannelListFragment.newInstance {
                    setFragment(CustomChannelListFragment())
                    customTheme(R.style.StreamUiTheme)
                    showSearch(true)
                    showHeader(true)
                    headerTitle("Title")
                }
            }
        }

        fun startActivity(context: Context) {
            context.startActivity(
                ChannelListActivity.createIntent(
                    context = context,
                    activityClass = CustomChannelListActivity::class.java
                )
            )
        }
    }
}
