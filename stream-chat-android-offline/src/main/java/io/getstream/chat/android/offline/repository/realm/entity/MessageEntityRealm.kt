package io.getstream.chat.android.offline.repository.realm.entity

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.MessageSyncType
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.repository.realm.utils.toDate
import io.getstream.chat.android.offline.repository.realm.utils.toRealmInstant
import io.getstream.chat.ui.sample.realm.entity.toDomain
import io.getstream.chat.ui.sample.realm.entity.toRealm
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

internal class MessageEntityRealm : RealmObject {
  @PrimaryKey
  var id: String = ""
  var cid: String = ""
  var user: UserEntityRealm? = null

  /** the message text */
  var text: String = ""

  /** the message text formatted as html **/
  var html: String = ""

  /** message type can be system regular or ephemeral */
  var type: String = ""

  /** if the message has been synced to the servers default is synced */
  var sync_status: Int = SyncStatus.COMPLETED.status

  var sync_type: MessageSyncType? = null

  /** the number of replies */
  var reply_count: Int = 0

  /** when the message was created */
  var created_at: RealmInstant? = null

  /** when the message was created locally */
  var created_locally_at: RealmInstant? = null

  /** when the message was updated */
  var updated_at: RealmInstant? = null

  /** when the message was updated locally */
  var updated_locally_at: RealmInstant? = null

  /** when the message was deleted */
  var deleted_at: RealmInstant? = null

  /** the users mentioned in this message */
  var remote_mentioned_user_ids: RealmList<String> = realmListOf()

  /** the users to be mentioned in this message */
  var mentioned_users_id: RealmList<String> = realmListOf()

  /** a mapping between reaction type and the count ie like:10 heart:4 */
  var reaction_counts: RealmList<ReactionCountEntityRealm> = realmListOf()

  /** a mapping between reaction type and the reaction score ie like:10 heart:4 */
  var reaction_scores: RealmList<ReactionScoreEntityRealm> = realmListOf()

  /** parent id used for threads */
  var parent_id: String? = null

  /** slash command like /giphy etc */
  var command: String? = null

  /** if the message was sent by shadow banned user */
  var shadowed: Boolean = false

  /** if the message is also shown in the channel **/
  var show_in_channel: Boolean = false
  // var channelInfo: ChannelInfoEntity? = null
  /** if the message is silent  **/
  var silent: Boolean = false

  /** all the custom data provided for this message */
  var extra_data: MutableMap<String, Any> = mutableMapOf()

  /** the ID of the quoted message **/
  var reply_to_id: String? = ""

  /** whether message is pinned or not **/
  var pinned: Boolean = false

  /** date when the message got pinned **/
  var pinned_at: RealmInstant? = null

  /** date when pinned message expires **/
  var pin_expires: RealmInstant? = null

  /** the ID of the user who pinned the message **/
  var pinned_by_user_id: String = ""

  /** participants of thread replies */
  var thread_participants_ids: RealmList<String> = realmListOf()
}

internal fun MessageEntityRealm.toDomain(): Message {
  return Message(
    id = this.id,
    cid = this.cid,
    user = this.user?.toDomain() ?: User(),
    text = this.text,
    html = this.html,
    command = this.command,
    mentionedUsersIds = this.mentioned_users_id,
    replyCount = this.reply_count,
    reactionCounts = this.reaction_counts.toDomain(),
    reactionScores = this.reaction_scores.toDomain(),
    syncStatus = this.sync_status.toDomain(),
    type = this.type,
    createdAt = this.created_at?.toDate(),
    updatedAt = this.updated_at?.toDate(),
    deletedAt = this.deleted_at?.toDate(),
    updatedLocallyAt = this.updated_locally_at?.toDate(),
    createdLocallyAt = this.created_locally_at?.toDate(),
    silent = this.silent,
    shadowed = this.shadowed,
    showInChannel = this.show_in_channel,
    replyMessageId = this.reply_to_id,
    pinned = this.pinned,
    pinnedAt = this.pinned_at?.toDate(),
    pinExpires = this.pin_expires?.toDate(),
  )
}

internal fun Message.toRealm(): MessageEntityRealm {
  val thisMessage = this
  return MessageEntityRealm().apply {
    this.id = thisMessage.id
    this.cid = thisMessage.cid
    this.user = thisMessage.user.toRealm()
    this.text = thisMessage.text
    this.html = thisMessage.html
    this.type = thisMessage.type
    this.sync_status = thisMessage.syncStatus.toRealm()
    this.created_at = thisMessage.createdAt?.toRealmInstant()
    this.created_locally_at = thisMessage.createdLocallyAt?.toRealmInstant()
    this.updated_at = thisMessage.updatedAt?.toRealmInstant()
    this.updated_locally_at = thisMessage.updatedLocallyAt?.toRealmInstant()
    this.deleted_at = thisMessage.deletedAt?.toRealmInstant()
    this.reaction_counts = thisMessage.reactionCounts.toReactionCountRealm()
    this.reaction_scores = thisMessage.reactionScores.toReactionScoreRealm()
    this.parent_id = thisMessage.parentId
    this.command = thisMessage.command
    this.shadowed = thisMessage.shadowed
    this.show_in_channel = thisMessage.showInChannel
    this.silent = thisMessage.silent
    this.extra_data = thisMessage.extraData
    this.pinned = thisMessage.pinned
    this.pinned_at = thisMessage.pinnedAt?.toRealmInstant()
    this.pin_expires = thisMessage.pinExpires?.toRealmInstant()
  }
}
