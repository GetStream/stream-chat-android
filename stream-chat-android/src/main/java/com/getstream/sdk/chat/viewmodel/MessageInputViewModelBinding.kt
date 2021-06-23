@file:JvmName("MessageInputViewModelBinding")

package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LifecycleOwner
import com.getstream.sdk.chat.view.messageinput.MessageInputView
import io.getstream.chat.android.client.models.Message
import java.io.File

/**
 * Binds [MessageInputView] with [MessageInputViewModel], updating the view's state
 * based on data provided by the ViewModel, and forwarding View events to the ViewModel.
 * This includes handling typing detection and sending messages.
 */
@JvmName("bind")
public fun MessageInputViewModel.bindView(view: MessageInputView, lifecycleOwner: LifecycleOwner) {
    view.messageSendHandler = object : MessageInputView.MessageSendHandler {
        override fun sendMessage(messageText: String) {
            this@bindView.sendMessage(messageText)
        }

        override fun sendMessageWithAttachments(
            message: String,
            attachmentsFilesWithMimeType: List<Pair<File, String?>>
        ) {
            this@bindView.sendMessageWithAttachments(message, attachmentsFilesWithMimeType)
        }

        override fun sendToThread(
            parentMessage: Message,
            messageText: String,
            alsoSendToChannel: Boolean,
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
            attachmentsFilesWithMimeType: List<Pair<File, String?>>,
        ) {
            this@bindView.sendMessageWithAttachments(message, attachmentsFilesWithMimeType) {
                this.parentId = parentMessage.id
                this.showInChannel = alsoSendToChannel
            }
        }

        override fun editMessage(oldMessage: Message, newMessageText: String) {
            this@bindView.editMessage(oldMessage.apply { text = newMessageText })
        }
    }
    view.addTypeListener(
        object : MessageInputView.TypeListener {
            override fun onKeystroke() = keystroke()
            override fun onStopTyping() = stopTyping()
        }
    )
    getActiveThread().observe(lifecycleOwner) { message ->
        if (message != null) {
            view.setThreadMode(message)
        } else {
            view.setNormalMode()
        }
    }
    @Suppress("DEPRECATION_ERROR")
    editMessage.observe(lifecycleOwner) { message ->
        if (message != null) {
            view.setEditMode(message)
        } else {
            view.setNormalMode()
        }
    }
    messageToEdit.observe(lifecycleOwner) { message ->
        if (message != null) {
            view.setEditMode(message)
        } else {
            view.setNormalMode()
        }
    }
    members.observe(lifecycleOwner) { view.configureMembers(it) }
    commands.observe(lifecycleOwner) { view.configureCommands(it) }
    maxMessageLength.observe(lifecycleOwner) { view.maxMessageLength = it }
}
