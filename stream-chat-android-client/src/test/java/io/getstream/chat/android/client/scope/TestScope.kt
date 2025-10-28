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

package io.getstream.chat.android.client.scope

import io.getstream.chat.android.client.scope.user.UserIdentifier
import io.getstream.chat.android.client.scope.user.UserJob
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.UserId
import io.getstream.result.call.SharedCalls
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestScope

internal class ClientTestScope(
    testScope: TestScope,
) : ClientScope,
    CoroutineScope by testScope + DispatcherProvider.IO + SharedCalls()

internal class UserTestScope(
    clientScope: ClientTestScope,
    userIdentifier: UserIdentifier = UserIdentifier(),
) : UserScope,
    CoroutineScope by (
        clientScope + userIdentifier + UserJob(clientScope.coroutineContext.job) {
            userIdentifier.value
        }
        ) {

    internal constructor(testScope: TestScope) : this(ClientTestScope(testScope))

    override val userId: UserIdentifier
        get() = coroutineContext[UserIdentifier] ?: error("no UserIdentifier found")

    override fun cancelChildren(userId: UserId?) {
        (coroutineContext[Job] as UserJob).cancelChildren(userId)
    }
}
