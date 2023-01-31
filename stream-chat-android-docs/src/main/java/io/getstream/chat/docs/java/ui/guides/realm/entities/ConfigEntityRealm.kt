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

package io.getstream.chat.docs.java.ui.guides.realm.entities

import io.getstream.chat.android.models.ChannelConfig
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.Config
import io.getstream.chat.docs.java.ui.guides.realm.utils.toDate
import io.getstream.chat.docs.java.ui.guides.realm.utils.toRealmInstant
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

private const val DEFAULT_MAX_MESSAGE: Int = 100

@Suppress("VariableNaming")
internal class ConfigEntityRealm : RealmObject {
    @PrimaryKey
    var channel_type: String = ""
    var created_at: RealmInstant? = null
    var updated_at: RealmInstant? = null
    var name: String = ""
    var is_typing_events: Boolean? = null
    var is_read_events: Boolean? = null
    var is_connect_events: Boolean? = null
    var is_search_enabled: Boolean? = null
    var is_reactions_enabled: Boolean? = null
    var is_thread_enabled: Boolean? = null
    var is_mutes_enabled: Boolean? = null
    var uploads_enabled: Boolean? = null
    var url_enrichment_enabled: Boolean? = null
    var custom_events_enabled: Boolean? = null
    var push_notifications_enabled: Boolean? = null
    var message_retention: String = ""
    var max_message_length: Int? = 0
    var automod: String = ""
    var automod_behavior: String = ""
    var blocklist_behavior: String = ""
    var commands: RealmList<CommandEntityRealm> = realmListOf()
}

@Suppress("VariableNaming")
internal class CommandEntityRealm : RealmObject {
    @PrimaryKey
    var id: Int = hashCode()
    var name: String = ""
    var description: String = ""
    var args: String = ""
    var set: String = ""
    var channel_type: String = ""
}

internal fun ConfigEntityRealm.toDomain(): ChannelConfig =
    ChannelConfig(
        type = channel_type,
        config = Config(
            createdAt = created_at?.toDate(),
            updatedAt = updated_at?.toDate(),
            name = name,
            typingEventsEnabled = is_typing_events ?: false,
            readEventsEnabled = is_read_events ?: false,
            connectEventsEnabled = is_connect_events ?: false,
            searchEnabled = is_search_enabled ?: false,
            isReactionsEnabled = is_reactions_enabled ?: false,
            isThreadEnabled = is_thread_enabled ?: false,
            muteEnabled = is_mutes_enabled ?: false,
            uploadsEnabled = uploads_enabled ?: false,
            urlEnrichmentEnabled = url_enrichment_enabled ?: false,
            customEventsEnabled = custom_events_enabled ?: false,
            pushNotificationsEnabled = push_notifications_enabled ?: false,
            messageRetention = message_retention,
            maxMessageLength = max_message_length ?: DEFAULT_MAX_MESSAGE,
            automod = automod,
            automodBehavior = automod_behavior,
            blocklistBehavior = blocklist_behavior,
            commands = commands.map { command -> command.toDomain() },
        )
    )

internal fun ChannelConfig.toRealm(): ConfigEntityRealm {
    val thisChannelConfig = this
    val thisConfig = this.config

    return ConfigEntityRealm().apply {
        channel_type = thisChannelConfig.type
        created_at = thisConfig.createdAt?.toRealmInstant()
        updated_at = thisConfig.updatedAt?.toRealmInstant()
        name = thisConfig.name
        is_typing_events = thisConfig.typingEventsEnabled
        is_read_events = thisConfig.isReactionsEnabled
        is_connect_events = thisConfig.connectEventsEnabled
        is_search_enabled = thisConfig.searchEnabled
        is_reactions_enabled = thisConfig.isReactionsEnabled
        is_thread_enabled = thisConfig.isThreadEnabled
        is_mutes_enabled = thisConfig.muteEnabled
        uploads_enabled = thisConfig.uploadsEnabled
        url_enrichment_enabled = thisConfig.urlEnrichmentEnabled
        custom_events_enabled = thisConfig.customEventsEnabled
        push_notifications_enabled = thisConfig.pushNotificationsEnabled
        message_retention = thisConfig.messageRetention
        max_message_length = thisConfig.maxMessageLength
        automod = thisConfig.automod
        automod_behavior = thisConfig.automodBehavior
        blocklist_behavior = thisConfig.blocklistBehavior
        commands = thisConfig.commands.map { command -> command.toRealm(channel_type) }.toRealmList()
    }
}

internal fun CommandEntityRealm.toDomain(): Command =
    Command(
        name = name,
        description = description,
        args = args,
        set = set
    )

internal fun Command.toRealm(channelType: String): CommandEntityRealm {
    val thisCommand = this

    return CommandEntityRealm().apply {
        name = thisCommand.name
        description = thisCommand.description
        args = thisCommand.args
        set = thisCommand.set
        channel_type = channelType
        id = hashCode()
    }
}
