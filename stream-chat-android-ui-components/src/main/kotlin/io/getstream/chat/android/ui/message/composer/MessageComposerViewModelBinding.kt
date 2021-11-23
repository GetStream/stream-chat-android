package io.getstream.chat.android.ui.message.composer

import androidx.lifecycle.LifecycleOwner

public fun MessageComposerViewModel.bindView(view: MessageComposerView, lifecycleOwner: LifecycleOwner) {
    view.onSendMessageAction = {
        val text = input.value
        val attachments = selectedAttachments.value
        val message = buildNewMessage(text, attachments)
        sendMessage(message)
    }
}
