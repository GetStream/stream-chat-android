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

package io.getstream.chat.android.extensions

import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Limits the float value to the given range.
 *
 * @param min The minimum value.
 * @param max The maximum value.
 */
@InternalStreamChatApi
public fun Float.limitTo(min: Float, max: Float): Float {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

/**
 * Checks if the float value is an integer.
 */
@InternalStreamChatApi
public fun Float.isInt(): Boolean {
    val diff = this - toInt()
    return diff <= 0
}
