package io.getstream.chat.android.ui.message.composer

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

public fun MessageComposerViewModel.bindView(
    view: MessageComposerView,
    lifecycleOwner: LifecycleOwner,
) {
    view.onSendMessage = {
        val message = this.buildNewMessage()
        sendMessage(message)
    }

    view.onInputChanged = {
        this.input.value = it
    }

    view.onClearInput = {
        this.setMessageInput("")
    }

    lifecycleOwner.lifecycleScope.launch {
        messageInputState.collect {
            view.renderState(it)
        }
    }
}
