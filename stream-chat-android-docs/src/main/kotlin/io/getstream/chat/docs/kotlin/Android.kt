package io.getstream.chat.docs.kotlin

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel
import io.getstream.chat.android.ui.channel.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.search.SearchInputView
import io.getstream.chat.android.ui.search.SearchResultListView
import io.getstream.chat.android.ui.search.SearchViewModel
import io.getstream.chat.android.ui.search.bindView

class Android {

    /**
     * @see <a href="https://getstream.io/chat/docs/android/channel_list_header_view">Channel List Header View</a>
     */
    class ChannelListHeader(private val channelListHeaderView: ChannelListHeaderView) : Fragment() {

        fun bindingWithViewModel() {
            // Get ViewModel
            val viewModel: ChannelListHeaderViewModel by viewModels()
            // Bind it with ChannelListHeaderView
            viewModel.bindView(channelListHeaderView, viewLifecycleOwner)
        }
    }

    /**
     * @see <a href="https://getstream.io/nessy/docs/chat_docs/android_chat_ux/search_input_view">Search Input View</a>
     */
    class SearchInput : Fragment() {
        lateinit var searchInputView: SearchInputView

        fun listeningForSearchQueryChanges() {
            searchInputView.setContinuousInputChangedListener {
                // Search query changed
            }
            searchInputView.setDebouncedInputChangedListener {
                // Search query changed and has been stable for a short while
            }
            searchInputView.setSearchStartedListener {
                // Search is triggered
            }

            // Update the current search query programmatically
            searchInputView.setQuery("query")
            // Clear the current search query programmatically
            searchInputView.clear()
        }
    }

    /**
     * @see <a href="https://getstream.io/nessy/docs/chat_docs/android_chat_ux/search_result_list_view">Search Result List View</a>
     */
    class SearchResultList : Fragment() {
        lateinit var searchInputView: SearchInputView
        lateinit var searchResultListView: SearchResultListView

        fun bindingWithViewModel() {
            // Get ViewModel
            val viewModel: SearchViewModel by viewModels()
            // Bind it with SearchResultListView
            viewModel.bindView(searchResultListView, viewLifecycleOwner)
            // Notify ViewModel when search is triggered
            searchInputView.setSearchStartedListener {
                viewModel.setQuery(it)
            }
        }
    }
}
