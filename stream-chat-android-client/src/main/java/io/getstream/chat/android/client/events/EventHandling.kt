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

package io.getstream.chat.android.client.events

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.models.Channel

/**
 * Handler responsible for deciding whether the set of channels should be updated after receiving the particular event.
 *
 * @see [EventHandlingResult]
 */
public fun interface ChatEventHandler {
    /**
     * Computes the event handling result.
     *
     * @param event [ChatEvent] that may contain updates for the set of channels.
     * @param filter [FilterObject] associated with the set of channels. Can be used to define the result of handling.
     * @param cachedChannel optional cached [Channel] object.
     *
     * @return [EventHandlingResult] Result of handling.
     */
    public fun handleChatEvent(event: ChatEvent, filter: FilterObject, cachedChannel: Channel?): EventHandlingResult
}

/**
 * Represent possible outcomes of handling a chat event.
 */
public sealed class EventHandlingResult {
    /**
     * Add a channel to a query channels collection.
     *
     * @param channel Channel to be added.
     */
    public data class Add(public val channel: Channel) : EventHandlingResult()

    /**
     * Call watch and add the channel to a query channels collection.
     *
     * @param cid cid of the channel to watch and add.
     */
    public data class WatchAndAdd(public val cid: String) : EventHandlingResult()

    /**
     * Remove a channel from a query channels collection.
     *
     * @param cid cid of channel to remove.
     *
     */
    public data class Remove(public val cid: String) : EventHandlingResult()

    /**
     * Skip the event.
     */
    public object Skip : EventHandlingResult() { override fun toString(): String = "Skip" }
}
