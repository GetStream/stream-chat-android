package io.getstream.chat.android.offline.repository.domain.channel.member.internal

import com.squareup.moshi.JsonClass
import java.util.Date

/**
 * Efficiently store the member data
 */
@JsonClass(generateAdapter = true)
internal data class MemberEntity(
    var userId: String,
    /** the user's role, user, moderator or admin */
    var role: String = "",

    /** when the user became a member */
    var createdAt: Date? = null,
    /** when the membership data was last updated */
    var updatedAt: Date? = null,

    /** if this is an invite */
    var isInvited: Boolean = false,

    /** the date the invite was accepted */
    var inviteAcceptedAt: Date? = null,

    /** the date the invite was rejected */
    var inviteRejectedAt: Date? = null,

    /** if channel member is shadow banned */
    var shadowBanned: Boolean = false,

    /** If channel member is banned. */
    var banned: Boolean = false,

    /** The user's channel-level role. */
    val channelRole: String? = null,
)
