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

@InternalStreamChatApi
public class SynchronizedReference<T : Any>(
    private var value: T? = null
) {

    public fun getOrCreate(builder: () -> T): T {
        return synchronized(this) {
            value ?: builder.invoke().also {
                value = it
            }
        }
    }

    public fun reset(): Boolean {
        return set(null) != null
    }

    public fun set(value: T?): T? {
        synchronized(this) {
            val currentValue = this.value
            this.value = value
            return currentValue
        }
    }
}
