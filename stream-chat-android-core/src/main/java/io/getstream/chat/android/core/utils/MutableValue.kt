/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.core.utils

import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Mutable value that tracks if it was modified.
 */
@InternalStreamChatApi
public class MutableValue<T>(initialValue: T) {

    private var modified = false
    private var currentValue: T = initialValue

    /**
     * Checks if the value is modified.
     */
    public fun isModified(): Boolean = modified

    /**
     * Provides the current value.
     */
    public fun get(): T = currentValue

    /**
     * Modifies the current value using [block] function.
     */
    public fun modify(block: (T) -> T) {
        set(block(currentValue))
    }

    /**
     * Sets the current value.
     */
    public fun set(value: T) {
        if (currentValue != value) {
            currentValue = value
            modified = true
        }
    }
}

@InternalStreamChatApi
public fun <T> MutableValue<T>.useIfModified(block: (T) -> Unit) {
    if (isModified()) {
        block(get())
    }
}

@InternalStreamChatApi
public fun <T> MutableValue<T?>.useNotNullIfModified(block: (T) -> Unit) {
    if (isModified()) {
        get()?.also(block)
    }
}
