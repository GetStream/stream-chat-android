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

package io.getstream.chat.android.client.api.models

@Suppress("UNCHECKED_CAST")
internal interface ChannelRequest<T : ChannelRequest<T>> {

    var state: Boolean
    var watch: Boolean
    var presence: Boolean

    fun withWatch(): T {
        watch = true
        return this as T
    }

    fun withState(): T {
        state = true
        return this as T
    }

    fun noWatch(): T {
        watch = false
        return this as T
    }

    fun noState(): T {
        state = false
        return this as T
    }

    fun withPresence(): T {
        presence = true
        return this as T
    }

    fun noPresence(): T {
        presence = false
        return this as T
    }
}
