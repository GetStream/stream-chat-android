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
public class StateHandlerBuilder<S : Any, E : Any, S1 : S> {
    @PublishedApi
    internal val eventHandlers: MutableMap<KClass<out E>, FiniteStateMachine<S, E>.(S1, E) -> S> = mutableMapOf()

    @FSMBuilderMarker
    public inline fun <reified E1 : E> onEvent(noinline func: FiniteStateMachine<S, E>.(S1, E1) -> S) {
        @Suppress("UNCHECKED_CAST")
        eventHandlers[E1::class] = func as FiniteStateMachine<S, E>.(S1, E) -> S
    }

    @PublishedApi
    @Suppress("UNCHECKED_CAST")
    internal fun get(): Map<KClass<out E>, StateFunction<S, E>> =
        eventHandlers as Map<KClass<out E>, StateFunction<S, E>>
}
