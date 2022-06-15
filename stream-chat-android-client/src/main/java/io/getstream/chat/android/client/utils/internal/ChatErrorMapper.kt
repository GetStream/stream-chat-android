package io.getstream.chat.android.client.utils.internal

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.errors.cause.MessageModerationFailedException
import io.getstream.chat.android.client.models.MessageModerationFailed
import io.getstream.chat.android.client.models.MessageSyncDescription
import io.getstream.chat.android.client.models.MessageSyncType
import io.getstream.chat.android.client.models.ModerationViolation
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public fun ChatError.toMessageSyncDescription(): MessageSyncDescription? {
    val networkError = this as? ChatNetworkError ?: return null
    return when (val cause = networkError.cause) {
        is MessageModerationFailedException -> MessageSyncDescription(
            type = MessageSyncType.FAILED_MODERATION,
            content = MessageModerationFailed(
                violations = cause.details.map { detail ->
                    ModerationViolation(detail.code, detail.messages)
                }
            )
        )
        else -> null
    }
}