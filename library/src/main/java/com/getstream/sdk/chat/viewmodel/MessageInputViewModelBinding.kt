package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.getstream.sdk.chat.view.MessageInputView
import io.getstream.chat.android.client.models.Message
import java.io.File

fun MessageInputViewModel.bindView(view: MessageInputView, lifecycleOwner: LifecycleOwner) {
    view.messageSendHandler = object : MessageInputView.MessageSendHandler {
        override fun sendMessage(messageText: String) {
            this@bindView.sendMessage(messageText)
        }

        override fun sendMessageWithAttachments(message: String, attachmentsFiles: List<File>) {
            this@bindView.sendMessageWithAttachments(message, attachmentsFiles)
        }

        override fun sendToThread(
            parentMessage: Message,
            messageText: String,
            alsoSendToChannel: Boolean
        ) {
            this@bindView.sendMessage(messageText) {
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
            this@bindView.sendMessageWithAttachments(message, attachmentsFiles) {
                this.parentId = parentMessage.id
                this.showInChannel = alsoSendToChannel
            }
        }

        override fun editMessage(oldMessage: Message, newMessageText: String) {
            this@bindView.editMessage(oldMessage.apply { text = newMessageText })
        }
    }
    view.addTypeListener(object : MessageInputView.TypeListener {
        override fun onKeystroke() = keystroke()
        override fun onStopTyping() = stopTyping()
    })
    getActiveThread().observe(
        lifecycleOwner,
        Observer {
            it?.let { view.setThreadMode(it) }
                ?: view.setNormalMode()
        }
    )
    editMessage.observe(
        lifecycleOwner,
        Observer {
            it?.let { view.setEditMode(it) }
                ?: view.setNormalMode()
        }
    )
    members.observe(lifecycleOwner, Observer { view.configureMembers(it) })
    commands.observe(lifecycleOwner, Observer { view.configureCommands(it) })
}
