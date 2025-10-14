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

package io.getstream.chat.android.models

import androidx.compose.runtime.Immutable
import io.getstream.chat.android.PrivacySettings
import io.getstream.chat.android.models.querysort.ComparableFieldProvider
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
 * @param privacySettings The privacy settings for the user.
 * @param banned Whether a user is banned or not.
 * @param devices The list of devices for the current user.
 * @param online Whether a is user online or not.
 * @param createdAt Date/time of creation.
 * @param updatedAt Date/time of the last update.
 * @param lastActive Date of last activity.
 * @param totalUnreadCount The total unread messages count for the current user.
 * @param unreadChannels The total unread channels count for the current user.
 * @param unreadThreads The total number of unread threads for the current user.
 * @param mutes A list of users muted by the current user.
 * @param teams List of teams user is a part of.
 * @param teamsRole The roles of the user in the teams they are part of. Example: `["teamId": "role"]`.
 * @param channelMutes A list of channels muted by the current user.
 * @param blockedUserIds A list of user ids blocked by the current user.
 * @param avgResponseTime The average time (in seconds) the user took to respond to messages.
 * @param pushPreference The push notification preference for the user.
 * @param extraData A map of custom fields for the user.
 * @param deactivatedAt Date/time of deactivation.
 */
@Immutable
public data class User(
    val id: String = "",
    val role: String = "",
    val name: String = "",
    val image: String = "",
    val invisible: Boolean? = null,
    val privacySettings: PrivacySettings? = null,
    val language: String = "",
    val banned: Boolean? = null,
    val devices: List<Device> = listOf(),
    val online: Boolean = false,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    val lastActive: Date? = null,
    val totalUnreadCount: Int = 0,
    val unreadChannels: Int = 0,
    val unreadThreads: Int = 0,
    val mutes: List<Mute> = listOf(),
    val teams: List<String> = listOf(),
    val teamsRole: Map<String, String> = emptyMap(),
    val channelMutes: List<ChannelMute> = emptyList(),
    val blockedUserIds: List<String> = emptyList(),
    val avgResponseTime: Long? = null,
    val pushPreference: PushPreference? = null,
    override val extraData: Map<String, Any> = mapOf(),
    val deactivatedAt: Date? = null,
) : CustomObject, ComparableFieldProvider {

    /**
     * Determines if the user is banned or not.
     */
    val isBanned: Boolean get() = banned == true

    /**
     * Determines if the user should share its online status.
     */
    val isInvisible: Boolean get() = invisible == true

    /**
     * Determines if the user has typing indicators enabled.
     */
    val isTypingIndicatorsEnabled: Boolean get() = privacySettings?.typingIndicators?.enabled ?: true

    /**
     * Determines if the user has read receipts enabled.
     */
    val isReadReceiptsEnabled: Boolean get() = privacySettings?.readReceipts?.enabled ?: true

    override fun getComparableField(fieldName: String): Comparable<*>? {
        return when (fieldName) {
            "id" -> id
            "role" -> role
            "name" -> name
            "image" -> image
            "invisible" -> invisible
            "language" -> language
            "banned" -> banned
            "online" -> online
            "total_unread_count", "totalUnreadCount" -> totalUnreadCount
            "unread_channels", "unreadChannels" -> unreadChannels
            "unread_threads", "unreadThreads" -> unreadThreads
            "created_at", "createdAt" -> createdAt
            "deactivated_at", "deactivatedAt" -> deactivatedAt
            "updated_at", "updatedAt" -> updatedAt
            "last_active", "lastActive" -> lastActive
            "avg_response_time", "avgResponseTime" -> avgResponseTime
            else -> extraData[fieldName] as? Comparable<*>
        }
    }

    @SinceKotlin("99999.9")
    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    public fun newBuilder(): Builder = Builder(this)

    public class Builder() {
        private var id: String = ""
        private var role: String = ""
        private var name: String = ""
        private var image: String = ""
        private var invisible: Boolean? = null
        private var privacySettings: PrivacySettings? = null
        private var language: String = ""
        private var banned: Boolean? = null
        private var devices: List<Device> = listOf()
        private var online: Boolean = false
        private var createdAt: Date? = null
        private var updatedAt: Date? = null
        private var lastActive: Date? = null
        private var totalUnreadCount: Int = 0
        private var unreadChannels: Int = 0
        private var unreadThreads: Int = 0
        private var mutes: List<Mute> = listOf()
        private var teams: List<String> = listOf()
        private var teamsRole: Map<String, String> = emptyMap()
        private var channelMutes: List<ChannelMute> = emptyList()
        private var blockedUserIds: List<String> = emptyList()
        private var avgResponseTime: Long? = null
        private var pushPreference: PushPreference? = null
        private var extraData: Map<String, Any> = mutableMapOf()
        private var deactivatedAt: Date? = null

        public constructor(user: User) : this() {
            id = user.id
            role = user.role
            name = user.name
            image = user.image
            invisible = user.invisible
            privacySettings = user.privacySettings
            language = user.language
            banned = user.banned
            devices = user.devices
            online = user.online
            createdAt = user.createdAt
            updatedAt = user.updatedAt
            lastActive = user.lastActive
            totalUnreadCount = user.totalUnreadCount
            unreadChannels = user.unreadChannels
            unreadThreads = user.unreadThreads
            mutes = user.mutes
            teams = user.teams
            teamsRole = user.teamsRole
            channelMutes = user.channelMutes
            blockedUserIds = user.blockedUserIds
            avgResponseTime = user.avgResponseTime
            pushPreference = user.pushPreference
            extraData = user.extraData
            deactivatedAt = user.deactivatedAt
        }
        public fun withId(id: String): Builder = apply { this.id = id }
        public fun withRole(role: String): Builder = apply { this.role = role }
        public fun withName(name: String): Builder = apply { this.name = name }
        public fun withImage(image: String): Builder = apply { this.image = image }
        public fun withInvisible(invisible: Boolean?): Builder = apply { this.invisible = invisible }
        public fun withPrivacySettings(privacySettings: PrivacySettings?): Builder =
            apply { this.privacySettings = privacySettings }
        public fun withLanguage(language: String): Builder = apply { this.language = language }
        public fun withBanned(banned: Boolean?): Builder = apply { this.banned = banned }
        public fun withDevices(devices: List<Device>): Builder = apply { this.devices = devices }
        public fun withOnline(online: Boolean): Builder = apply { this.online = online }
        public fun withCreatedAt(createdAt: Date?): Builder = apply { this.createdAt = createdAt }
        public fun withUpdatedAt(updatedAt: Date?): Builder = apply { this.updatedAt = updatedAt }
        public fun withLastActive(lastActive: Date?): Builder = apply { this.lastActive = lastActive }
        public fun withTotalUnreadCount(totalUnreadCount: Int): Builder = apply {
            this.totalUnreadCount = totalUnreadCount
        }
        public fun withUnreadChannels(unreadChannels: Int): Builder = apply { this.unreadChannels = unreadChannels }
        public fun withUnreadThreads(unreadThreads: Int): Builder = apply { this.unreadThreads = unreadThreads }
        public fun withMutes(mutes: List<Mute>): Builder = apply { this.mutes = mutes }
        public fun withTeams(teams: List<String>): Builder = apply { this.teams = teams }
        public fun withTeamsRole(teamsRole: Map<String, String>): Builder = apply { this.teamsRole = teamsRole }
        public fun withChannelMutes(channelMutes: List<ChannelMute>): Builder = apply {
            this.channelMutes = channelMutes
        }
        public fun withBlockedUserIds(blockedUserIds: List<String>): Builder = apply {
            this.blockedUserIds = blockedUserIds
        }
        public fun withAvgResponseTime(avgResponseTime: Long): Builder = apply {
            this.avgResponseTime = avgResponseTime
        }
        public fun withPushPreference(pushPreference: PushPreference?): Builder = apply {
            this.pushPreference = pushPreference
        }
        public fun withExtraData(extraData: Map<String, Any>): Builder = apply { this.extraData = extraData }
        public fun withDeactivatedAt(deactivatedAt: Date?): Builder = apply { this.deactivatedAt = deactivatedAt }

        public fun build(): User {
            return User(
                id = id,
                role = role,
                name = name,
                image = image,
                invisible = invisible,
                privacySettings = privacySettings,
                language = language,
                banned = banned,
                devices = devices,
                online = online,
                createdAt = createdAt,
                updatedAt = updatedAt,
                lastActive = lastActive,
                totalUnreadCount = totalUnreadCount,
                unreadChannels = unreadChannels,
                unreadThreads = unreadThreads,
                mutes = mutes,
                teams = teams,
                teamsRole = teamsRole,
                channelMutes = channelMutes,
                blockedUserIds = blockedUserIds,
                avgResponseTime = avgResponseTime,
                pushPreference = pushPreference,
                extraData = extraData.toMutableMap(),
                deactivatedAt = deactivatedAt,
            )
        }
    }
}
