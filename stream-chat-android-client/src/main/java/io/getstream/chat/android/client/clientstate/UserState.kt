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

package io.getstream.chat.android.client.clientstate

import io.getstream.chat.android.client.models.User as UserModel

internal sealed class UserState {
    object NotSet : UserState()
    class UserSet(val user: UserModel) : UserState()
    sealed class Anonymous : UserState() {
        object Pending : Anonymous()
        class AnonymousUserSet(val anonymousUser: UserModel) : Anonymous()
    }

    internal fun userOrError(): UserModel = when (this) {
        is UserSet -> user
        is Anonymous.AnonymousUserSet -> anonymousUser
        else -> error("This state doesn't contain user!")
    }
}
