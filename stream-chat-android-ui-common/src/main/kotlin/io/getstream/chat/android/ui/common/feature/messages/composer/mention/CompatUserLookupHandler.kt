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

package io.getstream.chat.android.ui.common.feature.messages.composer.mention

import io.getstream.chat.android.models.User
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Java compatibility interface for user lookup handler.
 */
public fun interface CompatUserLookupHandler {

    /**
     * Handles user lookup by given [query] in suspend way.
     * It's executed on background, so it can perform heavy operations.
     *
     * @param query String as user input for lookup algorithm.
     * @param callback The callback to be invoked when the user lookup is completed.
     * @return The cancel function to be invoked when the user lookup should be cancelled.
     */
    public fun handleCompatUserLookup(query: String, callback: (List<User>) -> Unit): () -> Unit
}

/**
 * Converts [CompatUserLookupHandler] to [UserLookupHandler].
 */
public fun CompatUserLookupHandler.toUserLookupHandler(): UserLookupHandler {
    return UserLookupHandler { query ->
        suspendCancellableCoroutine { cont ->
            val cancelable = handleCompatUserLookup(query) { users ->
                cont.resume(users)
            }
            cont.invokeOnCancellation {
                cancelable.invoke()
            }
        }
    }
}

public fun UserLookupHandler.toJavaCompatUserLookupHandler(): CompatUserLookupHandler {
    return CompatUserLookupHandler { query, callback ->
        runBlocking {
            val users = handleUserLookup(query)
            callback(users)
        }

        return@CompatUserLookupHandler {
        }
    }
}
