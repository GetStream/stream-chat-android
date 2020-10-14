package io.getstream.chat.android.livedata.entity

import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import java.util.Date

/**
 * Efficiently store the member data
 */
data class MemberEntity(var userId: String) {
    /** the user's role, user, moderator or admin */
    var role: String = ""

    /** when the user became a member */
    var createdAt: Date? = null
    /** when the membership data was last updated */
    var updatedAt: Date? = null

    /** if this is an invite */
    var isInvited = false

    /** the date the invite was accepted */
    var inviteAcceptedAt: Date? = null

    /** the date the invite was rejected */
    var inviteRejectedAt: Date? = null

    /** creates a memberEntity from the member */
    constructor(r: Member) : this(r.getUserId()) {
        role = r.role ?: r.user.role
        createdAt = r.createdAt
        updatedAt = r.updatedAt
        isInvited = r.isInvited ?: false
        inviteAcceptedAt = r.inviteAcceptedAt
        inviteRejectedAt = r.inviteRejectedAt
    }

    /** converts a member entity into a member */
    fun toMember(userMap: Map<String, User>): Member? {
        val user = userMap[userId]
        if (user == null) {
            ChatLogger.get("MemberEntity")
                .logE("userMap is missing the user with id='$userId` needed to create this member")
            return null
        }
        return Member(user, role, createdAt, updatedAt, isInvited, inviteAcceptedAt, inviteRejectedAt)
    }
}
