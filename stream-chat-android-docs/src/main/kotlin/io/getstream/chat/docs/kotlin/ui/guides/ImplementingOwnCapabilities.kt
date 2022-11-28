package io.getstream.chat.docs.kotlin.ui.guides

import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView

/**
 * [Implementing Own Capabilities](https://getstream.io/chat/docs/sdk/android/ui/guides/implementing-own-capabilities/)
 */
class ImplementingOwnCapabilities {

    private lateinit var messageComposerView: MessageComposerView

    fun customCapabilities() {
        val customOwnCapabilities = setOf(
            ChannelCapabilities.SEND_MESSAGE,
            ChannelCapabilities.SEND_LINKS,
            ChannelCapabilities.UPLOAD_FILE,
            ChannelCapabilities.SEND_TYPING_EVENTS
        )

        val messageComposerState = MessageComposerState(
            ownCapabilities = customOwnCapabilities
        )

        messageComposerView.renderState(messageComposerState)
    }
}
