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

package io.getstream.chat.android.offline.event.handler.internal.model

internal class ParameterizedLazy<T, R>(private val initializer: suspend (T) -> R) : suspend (T) -> R {
    @Volatile
    private var _value: R? = null

    override suspend fun invoke(param: T): R {
        if (_value == null) {
            _value = initializer(param)
        }
        return _value!!
    }

    fun isInitialized(): Boolean = _value != null
}

internal fun <T, R> parameterizedLazy(initializer: suspend (T) -> R): ParameterizedLazy<T, R> = ParameterizedLazy(
    initializer
)
