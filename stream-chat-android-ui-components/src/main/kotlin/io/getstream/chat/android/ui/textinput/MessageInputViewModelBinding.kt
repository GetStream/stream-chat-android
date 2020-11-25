@file:JvmName("MessageInputViewModelBinding")

package io.getstream.chat.android.ui.textinput

import androidx.lifecycle.LifecycleOwner
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import io.getstream.chat.android.client.models.Message
import java.io.File

/***
 * Binds [MessageInputView] with [MessageInputViewModel], updating the view's state
 * based on data provided by the ViewModel, and forwarding View events to the ViewModel.
 */
@JvmName("bind")
public fun MessageInputViewModel.bindView(view: MessageInputView, lifecycleOwner: LifecycleOwner) {
    members.observe(lifecycleOwner) { view.configureMembers(it) }
    commands.observe(lifecycleOwner) { view.configureCommands(it) }
    view.sendMessageHandler = object : MessageSendHandler {
        override fun sendMessage(messageText: String) {
            this@bindView.sendMessage(messageText)
        }

        override fun sendMessageWithAttachments(message: String, attachmentsFiles: List<File>) {
            TODO("Not yet implemented")
        }

        override fun sendToThread(parentMessage: Message, messageText: String, alsoSendToChannel: Boolean) {
            TODO("Not yet implemented")
        }

        override fun sendToThreadWithAttachments(
            parentMessage: Message,
            message: String,
            alsoSendToChannel: Boolean,
            attachmentsFiles: List<File>
        ) {
            TODO("Not yet implemented")
        }

        override fun editMessage(oldMessage: Message, newMessageText: String) {
            TODO("Not yet implemented")
        }
    }
}
