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

@file:Suppress("TooManyFunctions")

package io.getstream.chat.android.ui.feature.messages.list.internal

import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.utils.canBlockUser
import io.getstream.chat.android.ui.common.utils.canCopyMessage
import io.getstream.chat.android.ui.common.utils.canDeleteMessage
import io.getstream.chat.android.ui.common.utils.canEditMessage
import io.getstream.chat.android.ui.common.utils.canFlagMessage
import io.getstream.chat.android.ui.common.utils.canMarkAsUnread
import io.getstream.chat.android.ui.common.utils.canPinMessage
import io.getstream.chat.android.ui.common.utils.canReplyToMessage
import io.getstream.chat.android.ui.common.utils.canRetryMessage
import io.getstream.chat.android.ui.common.utils.canThreadReplyToMessage
import io.getstream.chat.android.ui.feature.messages.list.MessageListViewStyle

internal fun MessageListViewStyle.canReplyToMessage(
    message: Message,
    ownCapabilities: Set<String>,
): Boolean = canReplyToMessage(
    replyEnabled = replyEnabled,
    message = message,
    ownCapabilities = ownCapabilities,
)

internal fun MessageListViewStyle.canThreadReplyToMessage(
    message: Message,
    ownCapabilities: Set<String>,
): Boolean = canThreadReplyToMessage(
    threadsEnabled = threadsEnabled,
    message = message,
    ownCapabilities = ownCapabilities,
)

internal fun MessageListViewStyle.canCopyMessage(
    message: Message,
): Boolean = canCopyMessage(
    copyTextEnabled = copyTextEnabled,
    message = message,
)

internal fun MessageListViewStyle.canEditMessage(
    currentUser: User?,
    message: Message,
    ownCapabilities: Set<String>,
): Boolean = canEditMessage(
    editMessageEnabled = editMessageEnabled,
    currentUser = currentUser,
    message = message,
    ownCapabilities = ownCapabilities,
)

internal fun MessageListViewStyle.canDeleteMessage(
    currentUser: User?,
    message: Message,
    ownCapabilities: Set<String>,
): Boolean = canDeleteMessage(
    deleteMessageEnabled = deleteMessageEnabled,
    currentUser = currentUser,
    message = message,
    ownCapabilities = ownCapabilities,
)

internal fun MessageListViewStyle.canFlagMessage(
    currentUser: User?,
    message: Message,
    ownCapabilities: Set<String>,
): Boolean = canFlagMessage(
    flagEnabled = flagEnabled,
    currentUser = currentUser,
    message = message,
    ownCapabilities = ownCapabilities,
)

internal fun MessageListViewStyle.canPinMessage(
    message: Message,
    ownCapabilities: Set<String>,
): Boolean = canPinMessage(
    pinMessageEnabled = pinMessageEnabled,
    message = message,
    ownCapabilities = ownCapabilities,
)

internal fun MessageListViewStyle.canBlockUser(
    currentUser: User?,
    message: Message,
): Boolean = canBlockUser(
    blockUserEnabled = blockUserEnabled,
    currentUser = currentUser,
    message = message,
)

internal fun MessageListViewStyle.canMarkAsUnread(
    ownCapabilities: Set<String>,
): Boolean = canMarkAsUnread(
    markAsUnreadEnabled = markAsUnreadEnabled,
    ownCapabilities = ownCapabilities,
)

internal fun MessageListViewStyle.canRetryMessage(
    currentUser: User?,
    message: Message,
): Boolean = canRetryMessage(
    retryMessageEnabled = retryMessageEnabled,
    currentUser = currentUser,
    message = message,
)
