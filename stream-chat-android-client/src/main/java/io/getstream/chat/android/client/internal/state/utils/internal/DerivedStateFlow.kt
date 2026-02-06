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

package io.getstream.chat.android.client.internal.state.utils.internal

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Does not produce the same value in a raw, so respect "distinct until changed emissions"
 * https://github.com/Kotlin/kotlinx.coroutines/issues/2631#issuecomment-870565860
 * */
internal class DerivedStateFlow<T>(
    private val getValue: () -> T,
    private val flow: Flow<T>,
) : StateFlow<T> {

    override val replayCache: List<T>
        get() = listOf(value)

    override val value: T
        get() = getValue()

    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        coroutineScope { flow.distinctUntilChanged().stateIn(this).collect(collector) }
    }
}

internal fun <T1, R> StateFlow<T1>.mapState(transform: (a: T1) -> R): StateFlow<R> {
    return DerivedStateFlow(
        getValue = { transform(this.value) },
        flow = this.map { a -> transform(a) },
    )
}

internal fun <T1, T2, R> combineStates(
    flow: StateFlow<T1>,
    flow2: StateFlow<T2>,
    transform: (a: T1, b: T2) -> R,
): StateFlow<R> {
    return DerivedStateFlow(
        getValue = { transform(flow.value, flow2.value) },
        flow = combine(flow, flow2) { a, b -> transform(a, b) },
    )
}

internal fun <T1, T2, T3, R> combineStates(
    flow: StateFlow<T1>,
    flow2: StateFlow<T2>,
    flow3: StateFlow<T3>,
    transform: (a: T1, b: T2, c: T3) -> R,
): StateFlow<R> {
    return DerivedStateFlow(
        getValue = { transform(flow.value, flow2.value, flow3.value) },
        flow = combine(flow, flow2, flow3) { a, b, c -> transform(a, b, c) },
    )
}
