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

package io.getstream.chat.ui.sample.feature.chat.info

import io.getstream.chat.android.models.User
import java.io.Serializable
import java.util.Date

data class UserData(
    val id: String,
    val name: String,
    val image: String,
    val online: Boolean,
    val createdAt: Date?,
    val lastActive: Date?,
) : Serializable

fun UserData.toUser(): User = User().also { user ->
    user.id = id
    user.name = name
    user.image = image
    user.online = online
    user.createdAt = createdAt
    user.lastActive = lastActive
}

fun User.toUserData() = UserData(
    id = id,
    name = name,
    image = image,
    online = online,
    createdAt = createdAt,
    lastActive = lastActive,
)
