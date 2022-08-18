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

package io.getstream.chat.android.client.call

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

/**
 * Creates a shared [Call] instance.
 */
@Suppress("FunctionName", "UNCHECKED_CAST")
internal fun <T : Any> SharedCall(
    origin: Call<T>,
    originIdentifier: () -> String,
    scope: CoroutineScope,
): Call<T> {
    val sharedCalls = scope.coroutineContext[SharedCalls] ?: return origin
    val identifier = originIdentifier()
    return sharedCalls[identifier] as? Call<T>
        ?: DistinctCall(scope, { origin }) {
            sharedCalls.remove(identifier)
        }.also {
            sharedCalls.put(identifier, it)
        }
}

/**
 * The [CoroutineContext.Element] which holds ongoing calls until those get finished.
 */
@InternalStreamChatApi
public class SharedCalls : CoroutineContext.Element {

    /**
     * A key of [SharedCalls] coroutine context element.
     */
    public override val key: CoroutineContext.Key<SharedCalls> = Key

    /**
     * A collection of uncompleted calls.
     */
    private val calls = ConcurrentHashMap<String, Call<out Any>>()

    /**
     * Provides a [Call] based of specified [identifier] if available.
     */
    internal operator fun get(identifier: String): Call<out Any>? {
        return calls[identifier]
    }

    /**
     * Puts a [Call] behind of specified [identifier].
     */
    internal fun put(identifier: String, value: Call<out Any>) {
        calls[identifier] = value
    }

    /**
     * Removes a [Call] based of specified [identifier].
     */
    internal fun remove(identifier: String) {
        calls.remove(identifier)
    }

    public companion object Key : CoroutineContext.Key<SharedCalls>
}
