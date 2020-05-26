package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.getstream.sdk.chat.view.MessageInputView

fun MessageInputViewModel.bindView(view: MessageInputView, lifecycleOwner: LifecycleOwner) {
	view.setViewModel(this, lifecycleOwner)
	view.onSendMessageListener = object : MessageInputView.OnSendMessageListener {
		override fun onSendTextMessage(message: String) = sendMessage(message)
	}
	members.observe(lifecycleOwner, Observer { view.configureMembers(it) })
	commands.observe(lifecycleOwner, Observer { view.configureCommands(it) })
}