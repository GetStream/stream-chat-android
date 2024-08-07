package io.getstream.chat.docs.kotlin.compose.guides

import androidx.compose.runtime.Composable
import io.getstream.chat.android.compose.ui.messages.composer.MessageComposer
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState

/**
 * [Implementing Own Capabilities](https://getstream.io/chat/docs/sdk/android/compose/guides/implementing-own-capabilities/)
 */
class ImplementingOwnCapabilities {

    @Composable
    fun customOwnCapabilities() {
        val customOwnCapabilities = setOf(
            ChannelCapabilities.SEND_MESSAGE,
            ChannelCapabilities.SEND_LINKS,
            ChannelCapabilities.UPLOAD_FILE,
            ChannelCapabilities.SEND_TYPING_EVENTS,
            ChannelCapabilities.TYPING_EVENTS,
        )

        val messageComposerState = MessageComposerState(
            ownCapabilities = customOwnCapabilities
        )

        MessageComposer(
            messageComposerState = messageComposerState,
            onSendMessage = { _, _ -> }
        )
    }
}
