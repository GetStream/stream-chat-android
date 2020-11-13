@file:JvmName("MessageInputViewModelBinding")

package io.getstream.chat.android.ui.textinput

import androidx.lifecycle.LifecycleOwner
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel

/***
 * Binds [MessageInputView] with [MessageInputViewModel], updating the view's state
 * based on data provided by the ViewModel, and forwarding View events to the ViewModel.
 */
@JvmName("bind")
public fun MessageInputViewModel.bindView(view: MessageInputView, lifecycleOwner: LifecycleOwner) {
    members.observe(lifecycleOwner) { view.configureMembers(it) }
    commands.observe(lifecycleOwner) { view.configureCommands(it) }
}
