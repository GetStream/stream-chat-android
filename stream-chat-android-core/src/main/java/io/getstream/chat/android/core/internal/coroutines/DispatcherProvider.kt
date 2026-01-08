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

package io.getstream.chat.android.core.internal.coroutines

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider.reset
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider.set
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher

/**
 * Coroutine dispatchers used internally by Stream libraries. Should always be used
 * instead of directly using [Dispatchers] or creating new dispatchers.
 *
 * Can be modified using [set] and [reset] for testing purposes.
 */
@InternalStreamChatApi
public object DispatcherProvider {

    /**
     * Represents the Main coroutine dispatcher, tied to the UI thread.
     */
    public var Main: CoroutineDispatcher = Dispatchers.Main
        internal set

    /**
     * Represents the Immediate coroutine dispatcher, which is usually tied to the UI thread.
     *
     * Useful for some cases where the UI updates require immediate execution, without dispatching the update events.
     */
    public val Immediate: CoroutineDispatcher
        get() {
            val mainDispatcher = Main

            return if (mainDispatcher is MainCoroutineDispatcher) {
                mainDispatcher.immediate
            } else {
                mainDispatcher
            }
        }

    /**
     * Represents the IO coroutine dispatcher, which is usually tied to background work.
     */
    public var IO: CoroutineDispatcher = Dispatchers.IO
        internal set

    /**
     * Overrides the main (UI thread) and IO dispatcher. For testing purposes only.
     */
    public fun set(mainDispatcher: CoroutineDispatcher, ioDispatcher: CoroutineDispatcher) {
        Main = mainDispatcher
        IO = ioDispatcher
    }

    /**
     * Resets the dispatchers to their default values. For testing purposes only.
     */
    public fun reset() {
        Main = Dispatchers.Main
        IO = Dispatchers.IO
    }
}
