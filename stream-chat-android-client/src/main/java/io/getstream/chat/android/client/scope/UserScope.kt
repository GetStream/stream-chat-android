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

package io.getstream.chat.android.client.scope

import io.getstream.chat.android.client.scope.user.UserIdentifier
import io.getstream.chat.android.client.scope.user.UserJob
import io.getstream.chat.android.models.UserId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.plus

/**
 * A user aware implementation of [CoroutineScope].
 */
internal interface UserScope : CoroutineScope {

    /**
     * Returns [UserIdentifier] context element.
     */
    val userId: UserIdentifier

    /**
     * Cancels all children of the [UserJob] in this scope's context connected to the specified [userId].
     */
    fun cancelChildren(userId: UserId? = null)
}

/**
 * Creates a user aware [CoroutineScope].
 */
internal fun UserScope(clientScope: ClientScope): UserScope = UserScopeImpl(clientScope)

/**
 * Inherits [ClientScope] and adds elements such as [UserIdentifier] and [UserJob].
 */
private class UserScopeImpl(
    clientScope: ClientScope,
    userIdentifier: UserIdentifier = UserIdentifier(),
) : UserScope,
    CoroutineScope by (
        clientScope + userIdentifier + UserJob(clientScope.coroutineContext.job) { userIdentifier.value }
        ) {

    /**
     * Returns [UserIdentifier] context element.
     */
    override val userId: UserIdentifier
        get() = coroutineContext[UserIdentifier] ?: error("no UserIdentifier found")

    /**
     * Cancels all children of the [UserJob] in this scope's context connected to the specified [userId].
     */
    override fun cancelChildren(userId: UserId?) {
        (coroutineContext[Job] as UserJob).cancelChildren(userId)
    }
}
