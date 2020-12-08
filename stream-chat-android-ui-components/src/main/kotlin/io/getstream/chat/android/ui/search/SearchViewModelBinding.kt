@file:JvmName("SearchViewModelBinding")

package io.getstream.chat.android.ui.search

import androidx.lifecycle.LifecycleOwner

/**
 * Binds [SearchResultListView] with [SearchViewModel], updating the view's state
 * based on data provided by the ViewModel.
 */
@JvmName("bind")
public fun SearchViewModel.bindView(view: SearchResultListView, lifecycleOwner: LifecycleOwner) {
    state.observe(lifecycleOwner) { state ->
        when {
            state.isLoading -> {
                view.showLoading()
            }
            state.error != null -> {
                view.showError(state.error)
            }
            else -> {
                view.setMessages(state.query, state.results)
            }
        }
    }
    view.setLoadMoreListener {
        this.loadMore()
    }
}
