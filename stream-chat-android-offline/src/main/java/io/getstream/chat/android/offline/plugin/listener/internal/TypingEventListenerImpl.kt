/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.experimental.plugin.listeners.TypingEventListener
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.channel.internal.ChannelMutableState
import io.getstream.chat.android.offline.plugin.state.channel.internal.toMutableState
import java.util.Date

/**
 * [TypingEventListenerImpl] implementation for [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * Handles and sends typing events such as when user starts or stop typing a message.
 *
 * @param state [StateRegistry] having state of the offline plugin.
 */
internal class TypingEventListenerImpl(
    private val state: StateRegistry,
) : TypingEventListener {

    /**
     * Method called before original api request is invoked. If this methods returns [Result.error], API request is not invoked.
     *
     * @param eventType Type of the event that can be one of the [EventType.TYPING_START] or [EventType.TYPING_STOP] etc.
     * @param channelType Type of the channel in which the event is sent.
     * @param channelId Id of the channel in which the event is sent.
     * @param extraData Any extra data such as parent id.
     * @param eventTime [Date] object as the time of this event.
     *
     * @return [Result] having [Unit] if precondition passes otherwise [ChatError] describing what went wrong.
     */
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

    /**
     * Precondition method when user stops typing.
     *
     * To send stop typing event ([EventType.TYPING_STOP]), typing events must be enabled
     * and there should be a typing start event ([EventType.TYPING_START]) sent before [EventType.TYPING_STOP] can be sent.
     *
     * @param channelState State of the channel.
     */
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

    /**
     * Precondition method when user starts typing.
     *
     * To send start typing event ([EventType.TYPING_START]), typing events must be enabled
     * and there should be a delay of 3 seconds between two subsequents typing start event ([EventType.TYPING_START]).
     *
     * @param channelState State of the channel.
     * @param eventTime Time of this event.
     */
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

    /**
     * Side effect method which is called before sending any event.
     * Updates the local channel state about last typing event.
     *
     * @param eventType Type of the event that can be one of the [EventType.TYPING_START] or [EventType.TYPING_STOP] etc.
     * @param channelType Type of the channel in which the event is sent.
     * @param channelId Id of the channel in which the event is sent.
     * @param extraData Any extra data such as parent id.
     * @param eventTime [Date] object as the time of this event.
     */
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

    /**
     * Side effect method which is called after API request is completed.
     * Updates the local channel state about last typing event if original result is successful.
     *
     * @param result Result of the original request.
     * @param eventType Type of the event that can be one of the [EventType.TYPING_START] or [EventType.TYPING_STOP] etc.
     * @param channelType Type of the channel in which the event is sent.
     * @param channelId Id of the channel in which the event is sent.
     * @param extraData Any extra data such as parent id.
     * @param eventTime [Date] object as the time of this event.
     */
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
                        extraData[ARG_TYPING_PARENT_ID] as? String
                EventType.TYPING_STOP -> channelState.keystrokeParentMessageId = null
            }
        }
    }

    private companion object {
        private const val ARG_TYPING_PARENT_ID = "parent_id"
    }
}
