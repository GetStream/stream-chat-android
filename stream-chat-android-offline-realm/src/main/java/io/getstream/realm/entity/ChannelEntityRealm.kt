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

package io.getstream.realm.entity

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.ui.sample.realm.entity.toDomain
import io.getstream.chat.ui.sample.realm.entity.toRealm
import io.getstream.realm.utils.toDate
import io.getstream.realm.utils.toRealmInstant
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

@Suppress("VariableNaming")
internal class ChannelEntityRealm : RealmObject {
    @PrimaryKey
    var cid: String = ""
    var channel_id: String = ""
    var type: String = ""
    var name: String = ""
    var image: String = ""
    var cooldown: Int = 0
    var created_by: UserEntityRealm? = null
    var frozen: Boolean = false
    var hidden: Boolean? = null
    var hide_messages_before: RealmInstant? = null
    var member_count: Int = 0
    var messages: RealmList<MessageEntityRealm> = realmListOf()
    var members: RealmList<MemberEntityRealm> = realmListOf()
    var watchers: RealmList<UserEntityRealm> = realmListOf()
    var watcher_count: Int = 0
    var last_message_at: RealmInstant? = null
    var last_message_id: String = ""
    var reads: RealmList<ChannelUserReadEntityRealm> = realmListOf()
    var created_at: RealmInstant? = null
    var updated_at: RealmInstant? = null
    var deleted_at: RealmInstant? = null
    var extra_data: MutableMap<String, Any> = mutableMapOf()
    var sync_status: Int = SyncStatus.COMPLETED.status
    var team: String = ""
    var own_capabilities: Set<String> = emptySet()
    var membership: MemberEntityRealm? = null
}

internal fun Channel.toRealm(): ChannelEntityRealm {
    val thisChannel = this
    return ChannelEntityRealm().apply {
        cid = thisChannel.cid
        type = thisChannel.type
        channel_id = thisChannel.id
        name = thisChannel.name
        image = thisChannel.image
        cooldown = thisChannel.cooldown
        created_by = thisChannel.createdBy.toRealm()
        frozen = thisChannel.frozen
        hidden = thisChannel.hidden
        hide_messages_before = thisChannel.hiddenMessagesBefore?.toRealmInstant()
        messages = thisChannel.messages.map { message -> message.toRealm() }.toRealmList()
        member_count = thisChannel.memberCount
        members = thisChannel.members.map { member -> member.toRealm() }.toRealmList()
        watchers = thisChannel.watchers.map { it.toRealm() }.toRealmList()
        watcher_count = thisChannel.watcherCount
        last_message_at = thisChannel.lastMessageAt?.toRealmInstant()
        last_message_id = thisChannel.lastMessage()?.id ?: ""
        created_at = thisChannel.createdAt?.toRealmInstant()
        updated_at = thisChannel.updatedAt?.toRealmInstant()
        deleted_at = thisChannel.deletedAt?.toRealmInstant()
        extra_data = thisChannel.extraData
        sync_status = thisChannel.syncStatus.toRealm()
        team = thisChannel.team
        own_capabilities = thisChannel.ownCapabilities
    }
}

internal fun ChannelEntityRealm.toDomain(): Channel =
    Channel(
        cid = this.cid,
        id = this.channel_id,
        type = this.type,
        name = this.name,
        image = this.image,
        watcherCount = this.watcher_count,
        frozen = this.frozen,
        lastMessageAt = this.last_message_at?.toDate(),
        createdAt = this.created_at?.toDate(),
        deletedAt = this.deleted_at?.toDate(),
        updatedAt = this.updated_at?.toDate(),
        syncStatus = this.sync_status.toDomain(),
        members = members.map { it.toDomain() },
        memberCount = this.member_count,
        messages = messages.map { messageEntityRealm -> messageEntityRealm.toDomain() },
        createdBy = this.created_by?.toDomain() ?: User(),
        watchers = watchers.map { watcher -> watcher.toDomain() },
        team = this.team,
        read = reads.map { readEntity -> readEntity.toDomain() },
        hidden = this.hidden,
        hiddenMessagesBefore = this.hide_messages_before?.toDate(),
        cooldown = this.cooldown,
        ownCapabilities = this.own_capabilities,
        membership = this.membership?.toDomain(),
        extraData = this.extra_data
    )

internal fun Channel.lastMessage(): Message? = messages.lastOrNull()
