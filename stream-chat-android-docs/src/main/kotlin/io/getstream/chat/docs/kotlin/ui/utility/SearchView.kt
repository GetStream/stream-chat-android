// ktlint-disable filename

package io.getstream.chat.docs.kotlin.ui.utility

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.ui.feature.search.SearchInputView
import io.getstream.chat.android.ui.feature.search.list.SearchResultListView
import io.getstream.chat.android.ui.viewmodel.search.SearchViewModel
import io.getstream.chat.android.ui.viewmodel.search.bindView

/**
 * [Search View](https://getstream.io/chat/docs/sdk/android/ui/utility-components/search-view/)
 */
private class SearchView : Fragment() {

    lateinit var searchInputView: SearchInputView
    lateinit var searchResultListView: SearchResultListView

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/utility-components/search-view/#usage)
     */
    fun usage() {
        // Instantiate the ViewModel
        val viewModel: SearchViewModel by viewModels()
        // Bind it with SearchResultListView
        viewModel.bindView(searchResultListView, viewLifecycleOwner)
        // Notify ViewModel when search is triggered
        searchInputView.setSearchStartedListener(viewModel::setQuery)
    }

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/utility-components/search-view/#handling-actions)
     */
    fun handlingActions() {
        searchInputView.setContinuousInputChangedListener {
            // Search query changed
        }
        searchInputView.setDebouncedInputChangedListener {
            // Search query changed and has been stable for a short while
        }
        searchResultListView.setSearchResultSelectedListener { message ->
            // Handle search result click
        }
    }

    /**
     * [Updating Search Query Programmatically](https://getstream.io/chat/docs/sdk/android/ui/utility-components/search-view/#updating-the-search-query-programmatically)
     */
    fun updatingSearchQueryProgrammatically() {
        // Update the current search query programmatically
        searchInputView.setQuery("query")
        // Clear the current search query programmatically
        searchInputView.clear()
    }
}
