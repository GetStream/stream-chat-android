package io.getstream.chat.docs.cookbook.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.ui.search.SearchInputView
import io.getstream.chat.android.ui.search.list.SearchResultListView
import io.getstream.chat.android.ui.search.list.viewmodel.SearchViewModel
import io.getstream.chat.android.ui.search.list.viewmodel.bindView

/**
 * @see <a href="https://github.com/GetStream/stream-chat-android/wiki/UI-Cookbook#search-view">Search View</a>
 */
class SearchView : Fragment() {
    lateinit var searchInputView: SearchInputView
    lateinit var searchResultView: SearchResultListView

    fun bindingSearchViewComponents() {
        // Get view model
        val searchViewModel: SearchViewModel by viewModels()

        // Pass query to view model
        searchInputView.apply {
            setSearchStartedListener { query ->
                // Pass query when search was preformed
                searchViewModel.setQuery(query)
            }
            setDebouncedInputChangedListener { query ->
                // You can also track debounced search input changes
            }
            setContinuousInputChangedListener { query ->
                // You can also track continuous search input changes
            }
        }

        // Bind search result list view with view model
        searchViewModel.bindView(searchResultView, viewLifecycleOwner)

        // You can also handle search result clicks
        searchResultView.setSearchResultSelectedListener { message ->
            // Handle search result click
        }
    }
}
