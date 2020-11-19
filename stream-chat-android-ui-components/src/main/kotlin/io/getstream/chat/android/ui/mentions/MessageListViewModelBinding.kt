@file:JvmName("MentionsListViewModelBinding")

package io.getstream.chat.android.ui.mentions

import androidx.lifecycle.LifecycleOwner

/**
 * Binds [MentionsListView] with [MentionsListViewModel], updating the view's state
 * based on data provided by the ViewModel.
 */
@JvmName("bind")
public fun MentionsListViewModel.bindView(view: MentionsListView, lifecycleOwner: LifecycleOwner) {
    mentions.observe(lifecycleOwner) { messages ->
        view.setMessages(messages)
    }
}
