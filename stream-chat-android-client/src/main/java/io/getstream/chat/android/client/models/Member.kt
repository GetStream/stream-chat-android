package io.getstream.chat.android.client.models

import java.util.Date

/**
 * Represents a channel member.
 */
public data class Member(
    /**
     * The user who is a member of the channel.
     */
    override var user: User,

    /**
     * The user's role.
     */
    @Deprecated(
        level = DeprecationLevel.ERROR,
        message = "Use channelRole instead."
    )
    var role: String? = null,

    /**
     * When the user became a member.
     */
    var createdAt: Date? = null,

    /**
     * When the membership data was last updated.
     */
    var updatedAt: Date? = null,

    /**
     * If the user is invited.
     */
    var isInvited: Boolean? = null,

    /**
     * The date the invite was accepted.
     */
    var inviteAcceptedAt: Date? = null,

    /**
     * The date the invite was rejected.
     */
    var inviteRejectedAt: Date? = null,

    /**
     * If channel member is shadow banned.
     */
    var shadowBanned: Boolean = false,

    /**
     * If channel member is banned.
     */
    var banned: Boolean = false,

    /**
     * The user's channel-level role.
     */
    var channelRole: String? = null,
) : UserEntity
