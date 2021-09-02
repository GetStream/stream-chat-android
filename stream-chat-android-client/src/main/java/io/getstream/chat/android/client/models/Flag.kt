package io.getstream.chat.android.client.models

import java.util.Date

public data class Flag(
    val user: User,
    val targetUser: User?,
    val targetMessageId: String,
    val reviewedBy: String,
    val createdByAutomod: Boolean,
    val createdAt: Date,
    val updatedAt: Date,
    val reviewedAt: Date,
    val approvedAt: Date?,
    val rejectedAt: Date,
)
