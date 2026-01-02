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
import kotlin.reflect.KClass

internal typealias StateFunction<S, E> = (S, E) -> S

@InternalStreamChatApi
@FSMBuilderMarker
public class StateHandlerBuilder<STATE : Any, EVENT : Any, S : STATE> {

    @PublishedApi
    internal val eventHandlers: MutableMap<KClass<out EVENT>, StateFunction<STATE, EVENT>> = mutableMapOf()

    @PublishedApi
    internal val onEnterListeners: MutableList<(STATE, EVENT) -> Unit> = mutableListOf()

    @FSMBuilderMarker
    public inline fun <reified E : EVENT> onEvent(noinline func: S.(E) -> STATE) {
        @Suppress("UNCHECKED_CAST")
        eventHandlers[E::class] = func as (STATE, EVENT) -> STATE
    }

    @FSMBuilderMarker
    public inline fun onEnter(crossinline listener: S.(EVENT) -> Unit) {
        onEnterListeners.add { state, cause ->
            @Suppress("UNCHECKED_CAST")
            listener(state as S, cause)
        }
    }

    @PublishedApi
    internal fun get(): Map<KClass<out EVENT>, StateFunction<STATE, EVENT>> = eventHandlers

    @PublishedApi
    internal fun getEnterListeners(): MutableList<(STATE, EVENT) -> Unit> = onEnterListeners
}
