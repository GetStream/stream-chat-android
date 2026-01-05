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

package io.getstream.chat.android.core.internal.fsm.builder

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.fsm.FiniteStateMachine
import kotlin.reflect.KClass

@InternalStreamChatApi
@FSMBuilderMarker
public class FSMBuilder<STATE : Any, EVENT : Any> {
    private lateinit var _initialState: STATE
    public val stateFunctions: MutableMap<KClass<out STATE>, Map<KClass<out EVENT>, StateFunction<STATE, EVENT>>> =
        mutableMapOf()

    private var _defaultHandler: (STATE, EVENT) -> STATE = { s, _ -> s }

    @FSMBuilderMarker
    public fun initialState(state: STATE) {
        _initialState = state
    }

    @FSMBuilderMarker
    public fun defaultHandler(defaultHandler: (STATE, EVENT) -> STATE) {
        _defaultHandler = defaultHandler
    }

    @FSMBuilderMarker
    public inline fun <reified S : STATE> state(stateHandlerBuilder: StateHandlerBuilder<STATE, EVENT, S>.() -> Unit) {
        stateFunctions[S::class] = StateHandlerBuilder<STATE, EVENT, S>().apply(stateHandlerBuilder).get()
    }

    internal fun build(): FiniteStateMachine<STATE, EVENT> {
        check(this::_initialState.isInitialized) { "Initial state must be set!" }
        return FiniteStateMachine(_initialState, stateFunctions, _defaultHandler)
    }
}
