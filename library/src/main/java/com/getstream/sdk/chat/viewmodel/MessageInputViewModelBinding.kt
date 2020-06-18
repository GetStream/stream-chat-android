package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.getstream.sdk.chat.view.MessageInputView
import io.getstream.chat.android.client.models.Message
import java.io.File

fun MessageInputViewModel.bindView(view: MessageInputView, lifecycleOwner: LifecycleOwner) {
	view.setViewModel(this, lifecycleOwner)
	view.messageSendHandler = object : MessageInputView.MessageSendHandler {
		override fun sendMessage(messageText: String) {
			this@bindView.sendMessage(messageText)
		}

		override fun sendMessageWithAttachments(message: String, attachmentsFiles: List<File>) {
			this@bindView.sendMessageWithAttachments(message, attachmentsFiles)
		}

		override fun replyTo(parentMessage: Message, messageText: String) {
			this@bindView.sendMessage(messageText) { this.parentId = parentMessage.id }
		}

		override fun replyToWithAttachments(parentMessage: Message, message: String, attachmentsFiles: List<File>) {
			this@bindView.sendMessageWithAttachments(message, attachmentsFiles) { this.parentId = parentMessage.id }
		}
	}
	view.addTypeListener(object : MessageInputView.TypeListener {
		override fun onKeystroke() = keystroke()
		override fun onStopTyping() = stopTyping()
	})
	replyTo.observe(lifecycleOwner, Observer {
		it?.let { view.setReplyToMode(it) }
				?: view.setNormalMode()
	})
	members.observe(lifecycleOwner, Observer { view.configureMembers(it) })
	commands.observe(lifecycleOwner, Observer { view.configureCommands(it) })
}