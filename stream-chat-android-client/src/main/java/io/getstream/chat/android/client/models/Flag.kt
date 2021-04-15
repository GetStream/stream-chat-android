package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import java.util.Date

public data class Flag(
    val user: User,
    @SerializedName("target_user")
    val targetUser: User?,
    @SerializedName("target_message_id")
    val targetMessageId: String,
    @SerializedName("created_at")
    val reviewedBy: String,
    @SerializedName("created_by_automod")
    val createdByAutomod: Boolean,
    @SerializedName("approved_at")
    val createdAt: Date,
    @SerializedName("updated_at")
    val updatedAt: Date,
    @SerializedName("reviewed_at")
    val reviewedAt: Date,
    @SerializedName("reviewed_by")
    val approvedAt: Date?,
    @SerializedName("rejected_at")
    val rejectedAt: Date
)
