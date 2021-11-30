package io.getstream.chat.android.ui.message.composer

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import io.getstream.chat.android.client.models.Message
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

public fun MessageComposerViewModel.bindView(
    view: MessageComposerView,
    lifecycleOwner: LifecycleOwner,
    onSendMessage: (Message) -> Unit = { sendMessage(it) },
    onInputChanged: (String) -> Unit = { this.input.value = it },
    onDismissMessage: () -> Unit = { this.input.value = "" }
) {
    view.onSendMessageClickHandler = {
        val message = buildNewMessage()
        onSendMessage(message)
    }

    view.onInputChangedHandler = onInputChanged

    view.onDismissMessageHandler = onDismissMessage

    lifecycleOwner.lifecycleScope.launch {
        messageInputState.collect {
            view.renderState(it)
        }
    }
}
