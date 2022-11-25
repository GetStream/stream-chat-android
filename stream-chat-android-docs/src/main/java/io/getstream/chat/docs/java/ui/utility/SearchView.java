package io.getstream.chat.docs.java.ui.utility;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import io.getstream.chat.android.ui.feature.search.SearchInputView;
import io.getstream.chat.android.ui.feature.search.list.SearchResultListView;
import io.getstream.chat.android.ui.viewmodel.search.SearchViewModel;
import io.getstream.chat.android.ui.viewmodel.search.SearchViewModelBinding;

/**
 * [Search View](https://getstream.io/chat/docs/sdk/android/ui/utility-components/search-view/)
 */
public class SearchView extends Fragment {

    SearchInputView searchInputView;
    SearchResultListView searchResultListView;

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/utility-components/search-view/#usage)
     */
    public void usage() {
        // Instantiate the ViewModel
        SearchViewModel viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        // Bind it with SearchResultListView
        SearchViewModelBinding.bind(viewModel, searchResultListView, getViewLifecycleOwner());
        // Notify ViewModel when search is triggered
        searchInputView.setSearchStartedListener(viewModel::setQuery);
    }

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/utility-components/search-view/#handling-actions)
     */
    public void handlingActions() {
        searchInputView.setContinuousInputChangedListener(query -> {
            // Search query changed
        });
        searchInputView.setDebouncedInputChangedListener(query -> {
            // Search query changed and has been stable for a short while
        });
        searchResultListView.setSearchResultSelectedListener(message -> {
            // Handle search result click
        });
    }

    public void updatingSearchQueryProgrammatically() {
        // Update the current search query programmatically
        searchInputView.setQuery("query");
        // Clear the current search query programmatically
        searchInputView.clear();
    }
}
