package io.getstream.chat.android.ui.message.composer

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

public fun MessageComposerViewModel.bindView(view: MessageComposerView, lifecycleOwner: LifecycleOwner) {

    view.onSendMessageAction = {
        val text = input.value
        val attachments = selectedAttachments.value
        val message = buildNewMessage(text, attachments)
        sendMessage(message)
    }

    view.onInputTextChanged = { setMessageInput(it) }

    lifecycleOwner.lifecycle.coroutineScope.launch {
        input.collect { input ->
            view.messageInputState.value = view.messageInputState.value.copy(inputValue = input)
        }
    }
}
