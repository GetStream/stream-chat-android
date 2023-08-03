/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.offline.repository.domain.message.internal

import io.getstream.chat.android.models.MessageAwaitingAttachments
import io.getstream.chat.android.models.MessageModerationFailed
import io.getstream.chat.android.models.MessageSyncContent
import io.getstream.chat.android.models.MessageSyncNone
import io.getstream.chat.android.models.ModerationViolation

internal fun MessageSyncContentEntity.toModel(): MessageSyncContent {
    return when (this) {
        is MessageSyncNoneEntity -> MessageSyncNone
        is MessageModerationFailedEntity -> MessageModerationFailed(
            violations = violations.map { violation ->
                ModerationViolation(
                    code = violation.code,
                    messages = violation.messages,
                )
            },
        )
        is MessageAwaitingAttachmentsEntity -> MessageAwaitingAttachments
    }
}

internal fun MessageSyncContent.toEntity(): MessageSyncContentEntity {
    return when (this) {
        is MessageSyncNone -> MessageSyncNoneEntity()
        is MessageModerationFailed -> MessageModerationFailedEntity(
            violations = violations.map { violation ->
                MessageModerationFailedEntity.ViolationEntity(
                    code = violation.code,
                    messages = violation.messages,
                )
            },
        )
        is MessageAwaitingAttachments -> MessageAwaitingAttachmentsEntity()
    }
}
