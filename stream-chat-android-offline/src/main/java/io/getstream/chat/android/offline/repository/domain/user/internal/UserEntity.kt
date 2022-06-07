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

package io.getstream.chat.android.offline.repository.domain.user.internal

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * The UserEntity, id is a required field.
 *
 * You can convert a User object from the low level client to a UserEntity like this:
 * val userEntity = UserEntity(user)
 * and back:
 * userEntity.toUser()
 *
 * @param id The unique id of the user. This field if required.
 * @param originalId Used for storing the current user.
 * @param name User's name.
 * @param image User's image.
 * @param role Determines the set of user permissions.
 * @param createdAt Date/time of creation.
 * @param updatedAt Date/time of the last update.
 * @param lastActive Date of last activity.
 * @param invisible Determines if the user should share its online status. Can only be changed while connecting the user.
 * @param banned Whether a user is banned or not.
 * @param mutes A list of users muted by the current user.
 * @param extraData A map of custom fields for the user.
 */
@Entity(tableName = "stream_chat_user")
internal data class UserEntity(
    @PrimaryKey val id: String,
    val originalId: String = "",
    @ColumnInfo(index = true)
    val name: String,
    val image: String,
    val role: String = "",
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    val lastActive: Date? = null,
    val invisible: Boolean = false,
    val banned: Boolean = false,
    val mutes: List<String> = emptyList(),
    val extraData: Map<String, Any> = emptyMap()
)
