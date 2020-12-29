@file:JvmName("MessageInputViewModelBinding")

package io.getstream.chat.android.ui.textinput

import androidx.lifecycle.LifecycleOwner
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import io.getstream.chat.android.client.models.Message
import java.io.File

/**
 * Binds [MessageInputView] with [MessageInputViewModel], updating the view's state
 * based on data provided by the ViewModel, and forwarding View events to the ViewModel.
 */
@JvmName("bind")
public fun MessageInputViewModel.bindView(view: MessageInputView, lifecycleOwner: LifecycleOwner) {
    members.observe(lifecycleOwner, view::configureMembers)
    commands.observe(lifecycleOwner, view::configureCommands)
    maxMessageLength.observe(lifecycleOwner, view::setMaxMessageLength)
    getActiveThread().observe(lifecycleOwner) {
        view.inputMode = if (it != null) {
            MessageInputView.InputMode.Thread(it)
        } else {
            MessageInputView.InputMode.Normal
        }
    }
    editMessage.observe(lifecycleOwner) { message ->
        message?.let {
            view.inputMode = MessageInputView.InputMode.Edit(it)
        }
    }

    view.setSendMessageHandler(
        object : MessageInputView.MessageSendHandler {
            val viewModel = this@bindView
            override fun sendMessage(messageText: String) {
                viewModel.sendMessage(messageText)
            }

            override fun sendMessageWithAttachments(message: String, attachmentsFiles: List<File>) {
                viewModel.sendMessageWithAttachments(message, attachmentsFiles)
            }

            override fun sendToThread(parentMessage: Message, messageText: String, alsoSendToChannel: Boolean) {
                viewModel.sendMessage(messageText) {
                    this.parentId = parentMessage.id
                    this.showInChannel = alsoSendToChannel
                }
            }

            override fun sendToThreadWithAttachments(
                parentMessage: Message,
                message: String,
                alsoSendToChannel: Boolean,
                attachmentsFiles: List<File>
            ) {
                viewModel.sendMessageWithAttachments(message, attachmentsFiles) {
                    this.parentId = parentMessage.id
                    this.showInChannel = alsoSendToChannel
                }
            }

            override fun editMessage(oldMessage: Message, newMessageText: String) {
                viewModel.editMessage(oldMessage.copy(text = newMessageText))
            }
        }
    )
    view.setTypingListener(
        object : MessageInputView.TypingListener {
            override fun onKeystroke() = keystroke()
            override fun onStopTyping() = stopTyping()
        }
    )

    repliedMessage.observe(lifecycleOwner) {
        if (it != null) {
            view.inputMode = MessageInputView.InputMode.Reply(it)
        } else {
            view.inputMode = MessageInputView.InputMode.Normal
        }
    }
}
