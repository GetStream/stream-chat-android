package io.getstream.chat.android.offline.repository.domain.message.internal

import com.squareup.moshi.JsonClass

public sealed class MessageSyncContentEntity

public sealed class MessageSyncInProgressEntity : MessageSyncContentEntity()
public sealed class MessageSyncFailedEntity : MessageSyncContentEntity()

@JsonClass(generateAdapter = true)
public class MessageAwaitingAttachmentsEntity : MessageSyncInProgressEntity() {
    override fun toString(): String = "MessageAwaitingAttachmentsEntity"
}

@JsonClass(generateAdapter = true)
public data class MessageModerationFailedEntity(
    val violations: List<ViolationEntity>,
) : MessageSyncFailedEntity() {
    public data class ViolationEntity(
        val code: Int,
        val messages: List<String>,
    )
}