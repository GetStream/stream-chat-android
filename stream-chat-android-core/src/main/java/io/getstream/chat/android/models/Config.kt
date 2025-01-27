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
import java.util.Date

/**
 * Model representing the configuration of a channel.
 */
@Immutable
public data class Config(

    /**
     * Date of channel creation.
     */
    val createdAt: Date? = null,

    /**
     * Date of last channel update.
     */
    val updatedAt: Date? = null,

    /**
     * The name of the channel type must be unique per application
     */
    val name: String = "",

    /**
     * Controls if typing indicators are shown. Enabled by default.
     */
    val typingEventsEnabled: Boolean = true,

    /**
     * Controls whether the chat shows how far you’ve read. Enabled by default.
     */
    val readEventsEnabled: Boolean = true,

    /**
     * Determines if events are fired for connecting and disconnecting to a chat. Enabled by default.
     */
    val connectEventsEnabled: Boolean = true,

    /**
     * Controls if messages should be searchable (this is a premium feature). Disabled by default.
     */
    val searchEnabled: Boolean = true,

    /**
     * If users are allowed to add reactions to messages. Enabled by default.
     */
    val isReactionsEnabled: Boolean = true,

    /**
     * Enables message threads. Enabled by default.
     */
    val isThreadEnabled: Boolean = true,

    /**
     * Determines if users are able to mute other users. Enabled by default.
     */
    val muteEnabled: Boolean = true,

    /**
     * Allows image and file uploads within messages. Enabled by default.
     */
    val uploadsEnabled: Boolean = true,

    /**
     * Determines if URL enrichment enabled to show they as attachments. Enabled by default.
     */
    val urlEnrichmentEnabled: Boolean = true,

    /**
     * Determines if custom events are enabled. Disabled by default.
     */
    val customEventsEnabled: Boolean = false,

    /**
     * Determines if push notifications are enabled. Enabled by default.
     */
    val pushNotificationsEnabled: Boolean = true,

    /**
     * Determines if the last message update should be skipped for system messages. Disabled by default.
     */
    val skipLastMsgUpdateForSystemMsgs: Boolean = false,

    /**
     * Determines if polls are enabled. Disabled by default.
     */
    val pollsEnabled: Boolean = false,

    /**
     * A number of days or infinite. "Infinite" by default.
     */
    val messageRetention: String = "infinite",

    /**
     * The max message length. 5000 by default.
     */
    val maxMessageLength: Int = 5000,

    /**
     * Disabled, simple or AI are valid options for the Automod. AI based moderation is a premium feature.
     */
    val automod: String = "disabled",

    /**
     * Represents the automod behaviour.
     */
    val automodBehavior: String = "",

    /**
     * Represents the blocklist behaviour.
     */
    val blocklistBehavior: String = "",

    /**
     * The commands that are available on this channel type, e.g. /giphy.
     */
    val commands: List<Command> = mutableListOf(),
)
