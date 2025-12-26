package io.getstream.chat.android.state.plugin.logic.channel.internal

import io.getstream.chat.android.client.channel.ChannelMessagesUpdateLogic
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.channel.state.ChannelStateLogicProvider
import io.getstream.chat.android.client.utils.message.isPinned
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.state.plugin.state.channel.internal.ChannelStateImpl

/**
 * Implementation of [ChannelMessagesUpdateLogic] backed by [ChannelStateImpl].
 * Serves as the bridge between the LLC and the channel state for message updates. It is provided via the
 * [ChannelStateLogicProvider] to the LLC, when using the non-legacy state management plugin.
 *
 * @param state The [ChannelStateImpl] instance to interact with for state updates.
 * @param now A function that returns the current time in milliseconds.
 *
 * @see ChannelMessagesUpdateLogic
 * @see ChannelStateLogicProvider
 */
internal class ChannelMessagesUpdateLogicImpl(
    private val state: ChannelStateImpl,
    private val now: () -> Long,
) : ChannelMessagesUpdateLogic {

    override fun upsertMessage(message: Message) {
        state.upsertMessage(message)
    }

    // override fun upsertMessages(
    //     messages: List<Message>,
    //     shouldRefreshMessages: Boolean,
    // ) {
    //     if (shouldRefreshMessages) {
    //         state.setMessages(messages)
    //     } else {
    //         state.upsertMessages(messages)
    //     }
    // }

    // override fun upsertPinnedMessages(
    //     messages: List<Message>,
    //     shouldRefreshMessages: Boolean,
    // ) {
    //     if (shouldRefreshMessages) {
    //         // TODO: Does this make sense? state.setPinnedMessages(messages)
    //     } else {
    //         state.addPinnedMessages(messages)
    //     }
    // }

    // override fun delsertPinnedMessage(message: Message) {
    //     if (message.isPinned(now)) {
    //         state.addPinnedMessage(message)
    //     } else {
    //         state.deletePinnedMessage(message.id)
    //     }
    // }

    override fun listenForChannelState(): ChannelState {
        return state
    }

    override fun setRepliedMessage(repliedMessage: Message?) {
        state.setRepliedMessage(repliedMessage)
    }
}