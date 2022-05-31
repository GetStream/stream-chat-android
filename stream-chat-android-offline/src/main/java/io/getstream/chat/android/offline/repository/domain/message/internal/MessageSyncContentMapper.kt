package io.getstream.chat.android.offline.repository.domain.message.internal

import io.getstream.chat.android.client.models.MessageAwaitingAttachments
import io.getstream.chat.android.client.models.MessageModerationFailed
import io.getstream.chat.android.client.models.MessageSyncContent

internal fun MessageSyncContentEntity.toModel(): MessageSyncContent {
    return when (this) {
        is MessageModerationFailedEntity -> MessageModerationFailed(
            violations = violations.map { violation ->
                MessageModerationFailed.Violation(
                    code = violation.code,
                    messages = violation.messages
                )
            }
        )
        is MessageAwaitingAttachmentsEntity -> MessageAwaitingAttachments
    }
}

internal fun MessageSyncContent.toEntity(): MessageSyncContentEntity {
    return when (this) {
        is MessageModerationFailed -> MessageModerationFailedEntity(
            violations = violations.map { violation ->
                MessageModerationFailedEntity.ViolationEntity(
                    code = violation.code,
                    messages = violation.messages
                )
            }
        )
        is MessageAwaitingAttachments -> MessageAwaitingAttachmentsEntity()
    }
}