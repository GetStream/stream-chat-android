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

package io.getstream.chat.android.core.internal.concurrency

import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * An object reference that may be updated thread-safely.
 */
@InternalStreamChatApi
public class SynchronizedReference<T : Any>(
    @Volatile private var value: T? = null,
) {

    /**
     * Provides an existing [T] object reference.
     */
    public fun get(): T? = value

    /**
     * Provides either an existing [T] object or creates a new one using [builder] function.
     *
     * This method is **thread-safe** and can be safely invoked without external synchronization.
     */
    public fun getOrCreate(builder: () -> T): T = value ?: synchronized(this) {
        value ?: builder.invoke().also {
            value = it
        }
    }

    /**
     * Drops an existing [T] object reference to null.
     */
    public fun reset(): Boolean = set(null) != null

    /**
     * Accepts [value] instance of [T] and holds its reference.
     *
     * This method is **thread-safe** and can be safely invoked without external synchronization.
     */
    public fun set(value: T?): T? {
        synchronized(this) {
            val currentValue = this.value
            this.value = value
            return currentValue
        }
    }
}
