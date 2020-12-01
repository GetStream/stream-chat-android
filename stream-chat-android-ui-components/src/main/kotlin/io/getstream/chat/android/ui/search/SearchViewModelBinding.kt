@file:JvmName("SearchViewModelBinding")

package io.getstream.chat.android.ui.search

import androidx.lifecycle.LifecycleOwner

/**
 * Binds [SearchResultListView] with [SearchViewModel], updating the view's state
 * based on data provided by the ViewModel.
 */
@JvmName("bind")
public fun SearchViewModel.bindView(view: SearchResultListView, lifecycleOwner: LifecycleOwner) {
    results.observe(lifecycleOwner) { messages ->
        view.setMessages(messages)
    }
}
