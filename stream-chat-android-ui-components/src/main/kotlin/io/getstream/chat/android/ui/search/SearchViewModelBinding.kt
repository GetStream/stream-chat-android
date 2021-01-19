@file:JvmName("SearchViewModelBinding")

package io.getstream.chat.android.ui.search

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.livedata.utils.EventObserver

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
            else -> {
                view.showMessages(state.query, state.results)
            }
        }
    }
    errorEvents.observe(
        lifecycleOwner,
        EventObserver {
            view.showError()
        }
    )
    view.setLoadMoreListener {
        this.loadMore()
    }
}
