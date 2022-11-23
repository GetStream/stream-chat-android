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

package io.getstream.chat.android.state.event.handler.internal.model

import io.getstream.chat.android.models.User

/**
 * Represents currently logged in user.
 */
internal sealed class SelfUser {
    abstract val me: User
}

/**
 * Contains a full [User] information.
 */
internal data class SelfUserFull(override val me: User) : SelfUser()

/**
 * Contains a limited [User] information, like [User.id], [User.role], [User.name], [User.online], [User.banned],
 * [User.image], [User.createdAt], [User.updatedAt], [User.lastActive]
 */
internal data class SelfUserPart(override val me: User) : SelfUser()
