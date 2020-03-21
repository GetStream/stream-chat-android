package com.getstream.sdk.chat.livedata.entity

import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import java.util.*


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


    constructor(r: Member): this(r.getUserId()) {
        // TODO: finish this
        role = r.role
        createdAt = r.createdAt

    }

    /** converts a member entity into a member */
    fun toMember(userMap: Map<String, User>): Member {
        val r = Member()
        r.user = userMap.get(userId)!!
        // TODO: finish me

        return r

    }
}
