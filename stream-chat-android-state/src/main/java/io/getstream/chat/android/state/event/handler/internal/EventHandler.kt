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

package io.getstream.chat.android.state.event.handler.internal

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.events.ChatEvent

/**
 * Handles WebSocket and/or Synced events to update states and offline storage.
 */
internal interface EventHandler {

    /**
     * Triggers WebSocket event subscription.
     */
    fun startListening()

    /**
     * Cancels WebSocket event subscription.
     */
    fun stopListening()

    /**
     * For testing purpose only. Simulates socket event handling.
     */
    @VisibleForTesting
    suspend fun handleEvents(vararg events: ChatEvent)
}
