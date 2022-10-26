package io.getstream.chat.docs.java.ui.utility;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import io.getstream.chat.android.ui.search.SearchInputView;
import io.getstream.chat.android.ui.search.list.SearchResultListView;
import io.getstream.chat.android.ui.search.list.viewmodel.SearchViewModel;
import io.getstream.chat.android.ui.search.list.viewmodel.SearchViewModelBinding;

/**
 * [Search View](https://getstream.io/chat/docs/sdk/android/ui/utility-components/search-view/)
 */
public class SearchView extends Fragment {

    SearchInputView searchInputView;
    SearchResultListView searchResultListView;

    public void usage() {
        // Get ViewModel
        SearchViewModel viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        // Bind it with SearchResultListView
        SearchViewModelBinding.bind(viewModel, searchResultListView, getViewLifecycleOwner());
        // Notify ViewModel when search is triggered
        searchInputView.setSearchStartedListener(viewModel::setQuery);
    }

    public void handlingActions() {
        searchInputView.setContinuousInputChangedListener((query) -> {
            // Search query changed
        });
        searchInputView.setDebouncedInputChangedListener((query) -> {
            // Search query changed and has been stable for a short while
        });
        searchInputView.setSearchStartedListener((query) -> {
            // Search is triggered
        });
    }

    public void updatingSearchQueryProgrammatically() {
        // Update the current search query programmatically
        searchInputView.setQuery("query");
        // Clear the current search query programmatically
        searchInputView.clear();
    }
}
