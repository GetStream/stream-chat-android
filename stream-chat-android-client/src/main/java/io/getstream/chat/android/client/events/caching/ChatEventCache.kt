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

package io.getstream.chat.android.client.events.caching

import io.getstream.chat.android.client.events.ChatEvent

/**
 * A simple skeleton for a caching class.
 *
 * Designed for specific events that need to be cached or pruned
 * such as typing events.
 */
internal interface ChatEventCache {

    /**
     * Should process external events and decide
     * if they need to be fired or if another event
     * needs to be fired after a timer.
     */
    fun processEvent(event: ChatEvent)
}
