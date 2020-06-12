package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.getstream.sdk.chat.view.MessageInputView
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
	}
	view.addTypeListener(object : MessageInputView.TypeListener {
		override fun onKeystroke() = keystroke()
		override fun onStopTyping() = stopTyping()
	})
	members.observe(lifecycleOwner, Observer { view.configureMembers(it) })
	commands.observe(lifecycleOwner, Observer { view.configureCommands(it) })
}