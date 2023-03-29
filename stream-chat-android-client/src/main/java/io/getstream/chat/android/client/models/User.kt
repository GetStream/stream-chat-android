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

package io.getstream.chat.android.client.models

import io.getstream.chat.android.client.api.models.querysort.ComparableFieldProvider
import java.util.Date

/**
 * Represents a person who uses a chat and can perform chat operations like viewing channels or sending messages.
 *
 * @param id The unique id of the user. This field if required.
 * @param role Determines the set of user permissions.
 * @param name User's name.
 * @param image User's image.
 * @param invisible Determines if the user should share its online status. Can only be changed while connecting
 * the user.
 * @param banned Whether a user is banned or not.
 * @param devices The list of devices for the current user.
 * @param online Whether a is user online or not.
 * @param createdAt Date/time of creation.
 * @param updatedAt Date/time of the last update.
 * @param lastActive Date of last activity.
 * @param totalUnreadCount The total unread messages count for the current user.
 * @param unreadChannels The total unread channels count for the current user.
 * @param mutes A list of users muted by the current user.
 * @param teams List of teams user is a part of.
 * @param channelMutes A list of channels muted by the current user.
 * @param extraData A map of custom fields for the user.
 * @param deactivatedAt Date/time of deactivation.
 */
public data class User(
    var id: String = "",
    var role: String = "",
    var name: String = "",
    var image: String = "",
    var invisible: Boolean = false,
    var banned: Boolean = false,
    var devices: List<Device> = mutableListOf(),
    var online: Boolean = false,
    var createdAt: Date? = null,
    var updatedAt: Date? = null,
    var lastActive: Date? = null,
    var totalUnreadCount: Int = 0,
    var unreadChannels: Int = 0,
    var mutes: List<Mute> = mutableListOf(),
    val teams: List<String> = listOf(),
    val channelMutes: List<ChannelMute> = emptyList(),
    override var extraData: MutableMap<String, Any> = mutableMapOf(),
    val deactivatedAt: Date? = null,
) : CustomObject, ComparableFieldProvider {

    override fun getComparableField(fieldName: String): Comparable<*>? {
        return when (fieldName) {
            "id" -> id
            "role" -> role
            "name" -> name
            "image" -> image
            "invisible" -> invisible
            "banned" -> banned
            "online" -> online
            "totalUnreadCount" -> totalUnreadCount
            "unreadChannels" -> unreadChannels
            "createdAt" -> createdAt
            "deactivatedAt" -> deactivatedAt
            "updatedAt" -> updatedAt
            "lastActive" -> lastActive
            else -> extraData[fieldName] as? Comparable<*>
        }
    }
}
