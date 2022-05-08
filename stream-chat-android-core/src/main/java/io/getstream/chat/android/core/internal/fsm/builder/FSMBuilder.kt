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

package io.getstream.chat.android.core.internal.fsm.builder

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.fsm.FiniteStateMachine
import io.getstream.chat.android.core.internal.fsm.StateFunction
import kotlin.reflect.KClass

@InternalStreamChatApi
@FSMBuilderMarker
public class FSMBuilder<S : Any, E : Any> {
    private lateinit var _initialState: S
    public val stateFunctions: MutableMap<KClass<out S>, Map<KClass<out E>, StateFunction<S, E>>> = mutableMapOf()
    private var _defaultHandler: (S, E) -> S = { s, _ -> s }

    @FSMBuilderMarker
    public fun initialState(state: S) {
        _initialState = state
    }

    @FSMBuilderMarker
    public fun defaultHandler(defaultHandler: (S, E) -> S) {
        _defaultHandler = defaultHandler
    }

    @FSMBuilderMarker
    public inline fun <reified S1 : S> state(stateHandlerBuilder: StateHandlerBuilder<S, E, S1>.() -> Unit) {
        stateFunctions[S1::class] = StateHandlerBuilder<S, E, S1>().apply(stateHandlerBuilder).get()
    }

    internal fun build(): FiniteStateMachine<S, E> {
        check(this::_initialState.isInitialized) { "Initial state must be set!" }
        return FiniteStateMachine(_initialState, stateFunctions, _defaultHandler)
    }
}
