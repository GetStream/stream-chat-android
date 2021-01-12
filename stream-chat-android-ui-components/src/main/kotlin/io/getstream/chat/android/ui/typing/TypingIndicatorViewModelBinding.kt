@file:JvmName("TypingIndicatorViewModelBinding")

package io.getstream.chat.android.ui.typing

import androidx.lifecycle.LifecycleOwner

/**
 * Binds [TypingIndicatorView] with [TypingIndicatorViewModel], updating the view's state
 * based on data provided by the ViewModel.
 */
@JvmName("bind")
public fun TypingIndicatorViewModel.bindView(view: TypingIndicatorView, lifecycleOwner: LifecycleOwner) {
    typingUsers.observe(lifecycleOwner) { users ->
        view.setTypingUsers(users)
    }
}
