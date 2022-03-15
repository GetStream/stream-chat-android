// ktlint-disable filename

package io.getstream.chat.docs.kotlin.ui.utility

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.ui.search.SearchInputView
import io.getstream.chat.android.ui.search.list.SearchResultListView
import io.getstream.chat.android.ui.search.list.viewmodel.SearchViewModel
import io.getstream.chat.android.ui.search.list.viewmodel.bindView

/**
 * [Search View](https://getstream.io/chat/docs/sdk/android/ui/components/search-view/)
 */
private class SearchView : Fragment() {

    lateinit var searchInputView: SearchInputView
    lateinit var searchResultListView: SearchResultListView

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/components/search-view/#usage)
     */
    fun usage() {
        // Get ViewModel
        val viewModel: SearchViewModel by viewModels()
        // Bind it with SearchResultListView
        viewModel.bindView(searchResultListView, viewLifecycleOwner)
        // Notify ViewModel when search is triggered
        searchInputView.setSearchStartedListener {
            viewModel.setQuery(it)
        }
    }

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/components/search-view/#handling-actions)
     */
    fun handlingActions() {
        searchInputView.setContinuousInputChangedListener {
            // Search query changed
        }
        searchInputView.setDebouncedInputChangedListener {
            // Search query changed and has been stable for a short while
        }
        searchInputView.setSearchStartedListener {
            // Search is triggered
        }
    }

    /**
     * [Updating Search Query Programmatically](https://getstream.io/chat/docs/sdk/android/ui/components/search-view/#updating-the-search-query-programmatically)
     */
    fun updatingSearchQueryProgrammatically() {
        // Update the current search query programmatically
        searchInputView.setQuery("query")
        // Clear the current search query programmatically
        searchInputView.clear()
    }
}
