/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.util.extensions

import io.getstream.chat.android.compose.ui.components.messageoptions.MessageOptionItemVisibility
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User

internal fun MessageOptionItemVisibility.canReplyToMessage(
    message: Message,
    ownCapabilities: Set<String>,
): Boolean = io.getstream.chat.android.ui.common.utils.canReplyToMessage(
    replyEnabled = isReplyVisible,
    message = message,
    ownCapabilities = ownCapabilities,
)

internal fun MessageOptionItemVisibility.canThreadReplyToMessage(
    message: Message,
    ownCapabilities: Set<String>,
): Boolean = io.getstream.chat.android.ui.common.utils.canThreadReplyToMessage(
    threadsEnabled = isThreadReplyVisible,
    message = message,
    ownCapabilities = ownCapabilities,
)

internal fun MessageOptionItemVisibility.canCopyMessage(
    message: Message,
): Boolean = io.getstream.chat.android.ui.common.utils.canCopyMessage(
    copyTextEnabled = isCopyTextVisible,
    message = message,
)

internal fun MessageOptionItemVisibility.canEditMessage(
    currentUser: User?,
    message: Message,
    ownCapabilities: Set<String>,
): Boolean = io.getstream.chat.android.ui.common.utils.canEditMessage(
    editMessageEnabled = isEditMessageVisible,
    currentUser = currentUser,
    message = message,
    ownCapabilities = ownCapabilities,
)

internal fun MessageOptionItemVisibility.canDeleteMessage(
    currentUser: User?,
    message: Message,
    ownCapabilities: Set<String>,
): Boolean = io.getstream.chat.android.ui.common.utils.canDeleteMessage(
    deleteMessageEnabled = isDeleteMessageVisible,
    currentUser = currentUser,
    message = message,
    ownCapabilities = ownCapabilities,
)

internal fun MessageOptionItemVisibility.canFlagMessage(
    currentUser: User?,
    message: Message,
    ownCapabilities: Set<String>,
): Boolean = io.getstream.chat.android.ui.common.utils.canFlagMessage(
    flagEnabled = isFlagMessageVisible,
    currentUser = currentUser,
    message = message,
    ownCapabilities = ownCapabilities,
)

internal fun MessageOptionItemVisibility.canPinMessage(
    message: Message,
    ownCapabilities: Set<String>,
): Boolean = io.getstream.chat.android.ui.common.utils.canPinMessage(
    pinMessageEnabled = isPinMessageVisible,
    message = message,
    ownCapabilities = ownCapabilities,
)

internal fun MessageOptionItemVisibility.canBlockUser(
    currentUser: User?,
    message: Message,
): Boolean = io.getstream.chat.android.ui.common.utils.canBlockUser(
    blockUserEnabled = isBlockUserVisible,
    currentUser = currentUser,
    message = message,
)

internal fun MessageOptionItemVisibility.canMarkAsUnread(
    ownCapabilities: Set<String>,
): Boolean = io.getstream.chat.android.ui.common.utils.canMarkAsUnread(
    markAsUnreadEnabled = isMarkAsUnreadVisible,
    ownCapabilities = ownCapabilities,
)

internal fun MessageOptionItemVisibility.canRetryMessage(
    currentUser: User?,
    message: Message,
): Boolean = io.getstream.chat.android.ui.common.utils.canRetryMessage(
    retryMessageEnabled = isRetryMessageVisible,
    currentUser = currentUser,
    message = message,
)
