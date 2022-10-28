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

package io.getstream.chat.android.ui.common.extensions

import io.getstream.chat.android.client.models.AttachmentType
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.client.utils.message.isEphemeral
import io.getstream.chat.android.client.utils.message.isError
import io.getstream.chat.android.ui.ChatUI
import java.util.Date

public fun Message.isFailed(): Boolean {
    return this.syncStatus == SyncStatus.FAILED_PERMANENTLY || isError()
}

public fun Message.hasNoAttachments(): Boolean = attachments.isEmpty()

public fun Message.isGiphyNotEphemeral(): Boolean = isEphemeral().not() && command == AttachmentType.GIPHY

public fun Message.getCreatedAtOrNull(): Date? = createdAt ?: createdLocallyAt

public fun Message.getUpdatedAtOrNull(): Date? = updatedAt ?: updatedLocallyAt

public fun Message.getCreatedAtOrThrow(): Date = checkNotNull(getCreatedAtOrNull()) {
    "a message needs to have a non null value for either createdAt or createdLocallyAt"
}

public fun Message.hasSingleReaction(): Boolean {
    return supportedReactionCounts.size == 1
}

public fun Message.hasReactions(): Boolean {
    return supportedReactionCounts.isNotEmpty()
}

public val Message.supportedLatestReactions: List<Reaction>
    get() {
        return if (latestReactions.isEmpty()) {
            latestReactions
        } else {
            latestReactions.filter { ChatUI.supportedReactions.isReactionTypeSupported(it.type) }
        }
    }

public val Message.supportedReactionCounts: Map<String, Int>
    get() {
        return if (reactionCounts.isEmpty()) {
            reactionCounts
        } else {
            reactionCounts.filterKeys { ChatUI.supportedReactions.isReactionTypeSupported(it) }
        }
    }

public fun Message.hasText(): Boolean = text.isNotEmpty()
