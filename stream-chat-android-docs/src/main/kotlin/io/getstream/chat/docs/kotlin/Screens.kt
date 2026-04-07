package io.getstream.chat.docs.kotlin

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.ui.feature.channels.ChannelListActivity
import io.getstream.chat.android.ui.feature.channels.ChannelListFragment
import io.getstream.chat.android.ui.feature.channels.header.ChannelListHeaderView
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView
import io.getstream.chat.android.ui.feature.messages.ChannelActivity
import io.getstream.chat.android.ui.feature.messages.ChannelFragment
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView
import io.getstream.chat.android.ui.feature.messages.header.ChannelHeaderView
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.feature.search.SearchInputView
import io.getstream.chat.android.ui.feature.search.list.SearchResultListView
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.ui.R as UiR

class Screens {

    class ChannelListScreen {

        /**
         * Adding [ChannelListFragment] to your Activity
         */
        class MyChannelListActivity : AppCompatActivity() {

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(UiR.layout.stream_ui_fragment_container)

                if (savedInstanceState == null) {
                    supportFragmentManager.beginTransaction()
                        .replace(UiR.id.container, ChannelListFragment.newInstance())
                        .commit()
                }
            }
        }

        /**
         * Starting [ChannelListActivity] from the SDK
         */
        fun channelListActivity(context: Context) {
            context.startActivity(ChannelListActivity.createIntent(context))
        }

        /**
         * Implementing click listeners of [ChannelListFragment]
         */
        class MyChannelListActivityWithListeners :
            AppCompatActivity(),
            ChannelListFragment.HeaderActionButtonClickListener,
            ChannelListFragment.HeaderUserAvatarClickListener,
            ChannelListFragment.ChannelListItemClickListener,
            ChannelListFragment.SearchResultClickListener {

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                // Add ChannelListFragment to the layout
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

    class ChannelListScreenCustomization {

        /**
         * Using inheritance to customize [ChannelListActivity]
         */
        class CustomChannelListActivity : ChannelListActivity() {

            override fun createChannelListFragment(): ChannelListFragment {
                return ChannelListFragment.newInstance {
                    setFragment(CustomChannelListFragment())
                    customTheme(UiR.style.StreamUiTheme)
                    showSearch(true)
                    showHeader(true)
                    headerTitle("Title")
                }
            }
        }

        /**
         * Using inheritance to customize [ChannelListFragment]
         */
        class CustomChannelListFragment : ChannelListFragment() {

            override fun setupChannelListHeader(channelListHeaderView: ChannelListHeaderView) {
                super.setupChannelListHeader(channelListHeaderView)
                // Customize channel list header view. For example, set a custom avatar click listener:
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
                return ChannelListViewModel.DEFAULT_SORT
            }
        }
    }

    class MessageListScreen {

        /**
         * Adding [ChannelFragment] to your Activity
         */
        class MyChannelActivity : AppCompatActivity() {

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(UiR.layout.stream_ui_fragment_container)

                if (savedInstanceState == null) {
                    supportFragmentManager.beginTransaction()
                        .replace(UiR.id.container, ChannelFragment.newInstance(cid = "channelType:channelId") {})
                        .commit()
                }
            }
        }

        /**
         * Starting [ChannelActivity] from the SDK
         */
        fun messageListActivity(context: Context, cid: String, messageId: String?) {
            context.startActivity(ChannelActivity.createIntent(context, cid, messageId))
        }

        /**
         * Implementing click listeners of [ChannelFragment]
         */
        class MyChannelActivityWithListeners : AppCompatActivity(), ChannelFragment.BackPressListener {

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                // Add ChannelFragment to the layout
            }

            override fun onBackPress() {
                // Handle back press
            }
        }
    }

    class MessageListScreenCustomization {

        /**
         * Using inheritance to customize [ChannelActivity]
         */
        class CustomChannelActivity : ChannelActivity() {

            override fun createChannelFragment(cid: String, messageId: String?): ChannelFragment {
                return ChannelFragment.newInstance(cid) {
                    setFragment(CustomChannelFragment())
                    customTheme(UiR.style.StreamUiTheme)
                    showHeader(true)
                    messageId(messageId)
                }
            }
        }

        /**
         * Using inheritance to customize [ChannelFragment]
         */
        class CustomChannelFragment : ChannelFragment() {

            override fun setupChannelHeader(channelHeaderView: ChannelHeaderView) {
                super.setupChannelHeader(channelHeaderView)
                // Customize message list header view. For example, set a custom back button click listener:
                channelHeaderView.setBackButtonClickListener {
                    // Handle back press
                }
            }

            override fun setupMessageList(messageListView: MessageListView) {
                super.setupMessageList(messageListView)
                // Customize message list view
            }

            override fun setupMessageComposer(messageComposerView: MessageComposerView) {
                super.setupMessageComposer(messageComposerView)
                // Customize message composer view
            }
        }
    }
}
