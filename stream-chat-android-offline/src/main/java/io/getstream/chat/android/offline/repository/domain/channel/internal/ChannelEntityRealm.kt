package io.getstream.chat.android.offline.repository.domain.channel.internal

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.ui.sample.realm.entity.toDomain
import io.getstream.chat.ui.sample.realm.entity.toRealm
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.*

internal class ChannelEntityRealm : RealmObject {
  @PrimaryKey
  var channel_id: String = ""
  var type: String = ""
  var name: String = ""
  var image: String = ""
  var cooldown: Int = 0
  var created_by_user_id: String = ""
  var frozen: Boolean = false
  var hidden: Boolean? = null
  var hide_messages_before: Date? = null
  var member_Count: Int = 0
  var watcher_ids: MutableList<String> = mutableListOf()
  var watcher_count: Int = 0
  var last_message_at: Date? = null
  var last_message_id: String = ""
  var created_at: Date? = null
  var updated_at: Date? = null
  var deleted_at: Date? = null
  var extra_data: MutableMap<String, Any> = mutableMapOf()
  var sync_status: Int = SyncStatus.COMPLETED.status
  var team: String = ""
  var own_capabilities: Set<String> = emptySet()
}

internal fun Channel.toRealm(): ChannelEntityRealm {
  val thisChannel = this
  return ChannelEntityRealm().apply {
    type = thisChannel.type
    channel_id = thisChannel.id
    name = thisChannel.name
    image = thisChannel.image
    cooldown = thisChannel.cooldown
    created_by_user_id = thisChannel.createdBy.id
    frozen = thisChannel.frozen
    hidden = thisChannel.hidden
    hide_messages_before = thisChannel.hiddenMessagesBefore
    member_Count = thisChannel.memberCount
    watcher_ids = thisChannel.watchers.map { it.id }.toMutableList()
    watcher_count = thisChannel.watcherCount
    last_message_at = thisChannel.lastMessageAt
    last_message_id = thisChannel.lastMessage()?.id ?: ""
    created_at = thisChannel.createdAt
    updated_at = thisChannel.updatedAt
    deleted_at = thisChannel.deletedAt
    extra_data = thisChannel.extraData
    sync_status = thisChannel.syncStatus.toRealm()
    team = thisChannel.team
    own_capabilities = thisChannel.ownCapabilities
  }
}

internal fun ChannelEntityRealm.toDomain(): Channel =
  Channel(
    cid = "$type:$channel_id",
    id = this.channel_id,
    type = this.type,
    name = this.name,
    image = this.image,
    watcherCount = this.watcher_count,
    frozen = this.frozen,
    lastMessageAt = this.last_message_at,
    createdAt = this.created_at,
    deletedAt = this.deleted_at,
    updatedAt = this.updated_at,
    syncStatus = this.sync_status.toDomain(),
    memberCount = this.member_Count,
    messages = emptyList(),
    members = emptyList(),
    watchers = emptyList(),
    read = emptyList(),
//    createdBy = this.created_by_user_id,
//    unreadCount = this.unreadCount,
    team = this.team,
    hidden = this.hidden,
    hiddenMessagesBefore = this.hide_messages_before,
    cooldown = this.cooldown,
//    pinnedMessages = this.pi,
    ownCapabilities = this.own_capabilities,
//    membership = this.,
  )

internal fun Channel.lastMessage(): Message? = messages.lastOrNull()
