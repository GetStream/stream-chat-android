@file:JvmName("TypingIndicatorViewModelBinding")

package io.getstream.chat.android.ui.typing.viewmodel

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.ui.typing.TypingIndicatorView

/**
 * Binds [TypingIndicatorView] with [TypingIndicatorViewModel], updating the view's state
 * based on data provided by the ViewModel.
 *
 * This function sets listeners on the view and ViewModel. Call this method
 * before setting any additional listeners on these objects yourself.
 */
@JvmName("bind")
public fun TypingIndicatorViewModel.bindView(view: TypingIndicatorView, lifecycleOwner: LifecycleOwner) {
    typingUsers.observe(lifecycleOwner) { users ->
        view.setTypingUsers(users)
    }
}
