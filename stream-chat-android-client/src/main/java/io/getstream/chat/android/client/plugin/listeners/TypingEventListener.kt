/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.plugin.listeners

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.models.EventType
import io.getstream.result.Error
import io.getstream.result.Result
import java.util.Date

/**
 * Listener for [ChatClient.keystroke] and [ChatClient.stopTyping] requests.
 */
public interface TypingEventListener {

    /**
     * Runs this precondition before [ChatClient.keystroke] and [ChatClient.stopTyping] request is invoked.
     *
     * @param eventType Type of the event that can be one of the [EventType.TYPING_START] or
     * [EventType.TYPING_STOP] etc.
     * @param channelType Type of the channel in which the event is sent.
     * @param channelId Id of the channel in which the event is sent.
     * @param extraData Any extra data such as parent id.
     * @param eventTime [Date] object as the time of this event.
     *
     * @return [Result] having [Unit] if precondition passes otherwise [Error] describing what went wrong.
     */
    public fun onTypingEventPrecondition(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
        eventTime: Date,
    ): Result<Unit>

    /**
     * Runs this side effect before [ChatClient.keystroke] and [ChatClient.stopTyping] request is invoked.
     *
     * @param eventType Type of the event that can be one of the [EventType.TYPING_START] or
     * [EventType.TYPING_STOP] etc.
     * @param channelType Type of the channel in which the event is sent.
     * @param channelId Id of the channel in which the event is sent.
     * @param extraData Any extra data such as parent id.
     * @param eventTime [Date] object as the time of this event.
     */
    public fun onTypingEventRequest(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
        eventTime: Date,
    )

    /**
     * Runs this side effect after [ChatClient.keystroke] and [ChatClient.stopTyping] request is completed.
     *
     * @param result Result of the original request.
     * @param eventType Type of the event that can be one of the [EventType.TYPING_START] or
     * [EventType.TYPING_STOP] etc.
     * @param channelType Type of the channel in which the event is sent.
     * @param channelId Id of the channel in which the event is sent.
     * @param extraData Any extra data such as parent id.
     * @param eventTime [Date] object as the time of this event.
     */
    @Suppress("LongParameterList")
    public fun onTypingEventResult(
        result: Result<ChatEvent>,
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
        eventTime: Date,
    )
}
