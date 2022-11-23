package io.getstream.chat.android.state.plugin.logic.channel.internal

import io.getstream.chat.android.client.extensions.internal.shouldIncrementUnreadCount
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.buffer.StartStopBuffer
import io.getstream.chat.android.state.plugin.state.channel.internal.ChannelMutableState
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.state.utils.internal.isChannelMutedForCurrentUser
import io.getstream.logging.StreamLog

internal class UnreadCountLogic(
    private val mutableState: ChannelMutableState,
    private val globalMutableState: MutableGlobalState,
) {

    private val countBuffer: StartStopBuffer<Message> = StartStopBuffer(globalMutableState.queryingChannelsFree)

    init {
        countBuffer.subscribe(this::performCount)
    }

    /**
     * Increments the unread count of the Channel if necessary.
     *
     * @param message [Message].
     */
    fun incrementUnreadCountIfNecessary(message: Message) {
        countBuffer.enqueueData(message)
    }

    private fun performCount(message: Message) {
        val user = globalMutableState.user.value ?: return
        val currentUserId = user.id

        /* Only one thread can access this logic per time. If two messages pass the shouldIncrementUnreadCount at the
         * same time, one increment can be lost.
         */
        synchronized(this) {
            val readState = mutableState.read.value?.copy() ?: ChannelUserRead(user)
            val unreadCount: Int = readState.unreadMessages
            val lastMessageSeenDate = readState.lastMessageSeenDate

            // val isMessageAlreadyInState = mutableState.visibleMessages.value.containsKey(message.id)
            val shouldIncrementUnreadCount =
                // !isMessageAlreadyInState &&
                message.shouldIncrementUnreadCount(
                    currentUserId = currentUserId,
                    lastMessageAtDate = lastMessageSeenDate,
                    isChannelMuted = globalMutableState.isChannelMutedForCurrentUser(mutableState.cid)
                )

            if (shouldIncrementUnreadCount) {
                StreamLog.d(TAG) {
                    "It is necessary to increment the unread count for channel: " +
                        "${mutableState.channelData.value.id}. The last seen message was " +
                        "at: $lastMessageSeenDate. " +
                        "New unread count: ${unreadCount + 1}"
                }
                mutableState.increaseReadWith(message)
            }
        }
    }

    private companion object {
        private const val TAG = "UnreadCountLogic"
    }
}
