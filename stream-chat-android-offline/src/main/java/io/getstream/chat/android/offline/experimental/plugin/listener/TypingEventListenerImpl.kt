package io.getstream.chat.android.offline.experimental.plugin.listener

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.experimental.plugin.listeners.TypingEventListener
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.channel.state.ChannelMutableState
import io.getstream.chat.android.offline.experimental.channel.state.toMutableState
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry
import java.util.Date

@ExperimentalStreamChatApi
internal class TypingEventListenerImpl(
    private val state: StateRegistry,
) : TypingEventListener {

    override fun onTypingEventPrecondition(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
        eventTime: Date,
    ): Result<Unit> {
        val channelState = state.channel(channelType, channelId).toMutableState()
        return when (eventType) {
            EventType.TYPING_START -> {
                onTypingStartPrecondition(channelState, eventTime)
            }
            EventType.TYPING_STOP -> {
                onTypingStopPrecondition(channelState)
            }
            else -> Result.success(Unit)
        }
    }

    private fun onTypingStopPrecondition(channelState: ChannelMutableState): Result<Unit> {
        return if (!channelState.channelConfig.value.typingEventsEnabled)
            Result.error(ChatError("Typing events are not enabled"))
        else if (channelState.lastStartTypingEvent == null) {
            Result.error(
                ChatError(
                    "lastStartTypingEvent is null. " +
                        "Make sure to send Event.TYPING_START before sending Event.TYPING_STOP"
                )
            )
        } else Result.success(Unit)
    }

    private fun onTypingStartPrecondition(channelState: ChannelMutableState, eventTime: Date): Result<Unit> {
        return if (!channelState.channelConfig.value.typingEventsEnabled)
            Result.error(ChatError("Typing events are not enabled"))
        else if (channelState.lastStartTypingEvent != null && eventTime.time - channelState.lastStartTypingEvent!!.time < 3000) {
            Result.error(
                ChatError(
                    "Last typing event was sent at ${channelState.lastStartTypingEvent}. " +
                        "There must be a delay of 3 seconds before sending new event"
                )
            )
        } else Result.success(Unit)
    }

    override fun onTypingEventRequest(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
        eventTime: Date,
    ) {
        val channelState = state.channel(channelType, channelId).toMutableState()

        if (eventType == EventType.TYPING_START) {
            channelState.lastStartTypingEvent = eventTime
        } else if (eventType == EventType.TYPING_STOP) {
            channelState.lastStartTypingEvent = null
            channelState.lastKeystrokeAt = null
        }
    }

    override fun onTypingEventResult(
        result: Result<ChatEvent>,
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
        eventTime: Date,
    ) {
        if (result.isSuccess) {
            val channelState = state.channel(channelType, channelId).toMutableState()

            when (eventType) {
                EventType.TYPING_START ->
                    channelState.keystrokeParentMessageId =
                        extraData.getOrDefault(ARG_TYPING_PARENT_ID, null)?.toString()
                EventType.TYPING_STOP -> channelState.keystrokeParentMessageId = null
            }
        }
    }

    private companion object {
        private const val ARG_TYPING_PARENT_ID = "parent_id"
    }
}
