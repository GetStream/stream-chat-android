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

package io.getstream.chat.android.compose.util.extensions

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Converts a [StateFlow] to compose [State].
 *
 * @param coroutineScope The [CoroutineScope] in which to launch the coroutine in.
 */
internal fun <T> StateFlow<T>.asState(coroutineScope: CoroutineScope): State<T> {
    val state = mutableStateOf(this.value)
    onEach { state.value = it }.launchIn(coroutineScope)
    return state
}

/**
 * Converts a [Flow] to compose [State].
 *
 * @param coroutineScope The [CoroutineScope] in which to launch the coroutine in.
 */
internal fun <T> Flow<T>.asState(coroutineScope: CoroutineScope, defaultValue: T): State<T> {
    val state = mutableStateOf(defaultValue)
    onEach { state.value = it }.launchIn(coroutineScope)
    return state
}
