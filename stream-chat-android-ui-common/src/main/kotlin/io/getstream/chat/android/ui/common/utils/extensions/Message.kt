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

package io.getstream.chat.android.ui.common.utils.extensions

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageModerationAction
import io.getstream.chat.android.models.ModerationAction
import io.getstream.chat.android.models.User

/**
 * @return if the message was sent by current user.
 */
@InternalStreamChatApi
public fun Message.isMine(chatClient: ChatClient): Boolean = chatClient.clientState.user.value?.id == user.id

@InternalStreamChatApi
public fun Message.isMine(currentUser: User?): Boolean = currentUser?.id == user.id

/**
 * @return if the message failed at moderation or not.
 */
@Deprecated(
    message = "Use the one from stream-chat-android-client",
    replaceWith = ReplaceWith(
        expression = "isModerationFailed(currentUserId)",
        imports = ["io.getstream.chat.android.client.utils.message.isModerationError"],
    ),
    level = DeprecationLevel.WARNING,
)
public fun Message.isModerationFailed(currentUser: User?): Boolean = isMine(currentUser) &&
    type == Message.TYPE_ERROR &&
    (moderationDetails?.action == MessageModerationAction.bounce || moderation?.action == ModerationAction.bounce)
