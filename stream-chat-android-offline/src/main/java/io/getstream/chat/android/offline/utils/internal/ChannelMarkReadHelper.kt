package io.getstream.chat.android.offline.utils.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.channel.internal.toMutableState
import io.getstream.chat.android.offline.plugin.state.global.GlobalState

/**
 * Checks if the channel can be marked as read and marks it locally if needed.
 *
 * @param chatClient [ChatClient]
 * @param logic [LogicRegistry]
 * @param state [StateRegistry]
 * @param globalState [GlobalState]
 */
internal class ChannelMarkReadHelper(
    private val chatClient: ChatClient,
    private val logic: LogicRegistry,
    private val state: StateRegistry,
    private val globalState: GlobalState,
) {

    private val logger = ChatLogger.get("ChannelMarkReadHelper")

    /**
     * Marks channel as read locally if different conditions are met:
     * 1. Channel has read events enabled
     * 2. Channel has messages not marked as read yet
     * 3. Current user is set
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     *
     * @return The flag to determine if the channel was marked as read locally.
     */
    internal fun markChannelReadLocallyIfNeeded(channelType: String, channelId: String): Boolean {
        val channelState = state.channel(channelType = channelType, channelId = channelId).toMutableState()
        if (!channelState.channelConfig.value.readEventsEnabled) {
            return false
        }

        // throttle the mark read
        val messages = channelState.sortedMessages.value

        if (messages.isEmpty()) {
            logger.logI("No messages; nothing to mark read.")
            return false
        }

        return messages.last().createdAt
            .let { lastMessageDate ->
                val shouldUpdate =
                    channelState.lastMarkReadEvent == null || lastMessageDate?.after(channelState.lastMarkReadEvent) == true

                if (!shouldUpdate) {
                    logger.logI("Last message date [$lastMessageDate] is not after last read event [${channelState.lastMarkReadEvent}]; no need to update.")
                    return false
                }

                val currentUser = chatClient.getCurrentUser()

                if (currentUser == null) {
                    logger.logI("Cannot mark read because user is not set!")
                    return false
                }

                if (!globalState.isOnline()) {
                    logger.logI("Cannot mark read because user is offline!")
                    return false
                }

                channelState.lastMarkReadEvent = lastMessageDate

                // update live data with new read
                logic.channel(channelType = channelType, channelId = channelId)
                    .updateReads(listOf(ChannelUserRead(currentUser, channelState.lastMarkReadEvent)))

                true
            }
    }
}
