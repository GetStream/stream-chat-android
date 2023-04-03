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

package io.getstream.chat.android.client.utils.internal

import io.getstream.chat.android.client.errors.cause.MessageModerationFailedException
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.MessageModerationFailed
import io.getstream.chat.android.models.MessageSyncDescription
import io.getstream.chat.android.models.MessageSyncType
import io.getstream.chat.android.models.ModerationViolation
import io.getstream.result.Error

@InternalStreamChatApi
public fun Error.toMessageSyncDescription(): MessageSyncDescription? {
    val networkError = this as? Error.NetworkError ?: return null
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
