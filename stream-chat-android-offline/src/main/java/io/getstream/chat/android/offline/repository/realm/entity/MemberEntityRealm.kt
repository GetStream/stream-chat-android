package io.getstream.chat.android.offline.repository.realm.entity

import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.Date

internal class MemberEntityRealm : RealmObject {
    @PrimaryKey
    var user_id: String = ""

    var user: UserEntityRealm? = null

    /** the user's role, user, moderator or admin */
    var role: String = ""

    /** when the user became a member */
    var created_at: Date? = null

    /** when the membership data was last updated */
    var updated_at: Date? = null

    /** if this is an invite */
    var is_invited: Boolean = false

    /** the date the invite was accepted */
    var invite_accepted_at: Date? = null

    /** the date the invite was rejected */
    var invite_rejected_at: Date? = null

    /** if channel member is shadow banned */
    var shadow_banned: Boolean = false

    /** If channel member is banned. */
    var banned: Boolean = false

    /** The user's channel-level role. */
    var channel_role: String? = null
}

internal fun Member.toRealm(): MemberEntityRealm {  
    val thisMember = this

    return MemberEntityRealm().apply {
        user_id = thisMember.user.id
        user = thisMember.user.toRealm()
        created_at = thisMember.createdAt
        updated_at = thisMember.updatedAt
        is_invited = thisMember.isInvited ?: false
        invite_accepted_at = thisMember.inviteAcceptedAt
        invite_rejected_at = thisMember.inviteRejectedAt
        shadow_banned = thisMember.shadowBanned
        banned = thisMember.banned
        channel_role = thisMember.channelRole
    }
}

internal fun MemberEntityRealm.toDomain(): Member =
    Member(
        user = user?.toDomain() ?: User(),
        createdAt = created_at,
        updatedAt = updated_at,
        isInvited = is_invited,
        inviteAcceptedAt = invite_accepted_at,
        inviteRejectedAt = invite_rejected_at,
        shadowBanned = shadow_banned,
        banned = banned,
        channelRole = channel_role,
    )
