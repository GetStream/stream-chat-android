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

package io.getstream.chat.android.core.internal.lazy

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A [lazy] delegate which takes a parameter and provides a value based on it.
 * The value is calculated only once and stored in a map.
 *
 * @param initializer A function that creates a new value based on the parameter.
 */
@InternalStreamChatApi
public class ParameterizedLazy<T, R>(
    private val initializer: suspend (T) -> R,
) : suspend (T) -> R {

    private var values = hashMapOf<T, R>()

    private val mutex = Mutex()

    /**
     * Provides either an existing [R] object or creates a new one using [initializer] function.
     */
    override suspend fun invoke(param: T): R = values[param] ?: mutex.withLock {
        values[param] ?: initializer(param).also {
            values[param] = it
        }
    }
}

/**
 * Creates a [ParameterizedLazy] delegate from the provided [initializer] function.
 */
@InternalStreamChatApi
public fun <T, R> parameterizedLazy(initializer: suspend (T) -> R): ParameterizedLazy<T, R> = ParameterizedLazy(
    initializer,
)
