/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.components.messageoptions

import io.getstream.chat.android.compose.ui.components.selectedmessage.SelectedMessageMenu

/**
 * Controls option visibility in the message options menu. All options are visible by default.
 *
 * @param isRetryMessageVisible Visibility of the retry failed message option.
 * @param isReplyVisible Visibility of the reply to message option (quote message).
 * @param isThreadReplyVisible Visibility of the reply to message in thread option.
 * @param isMarkAsUnreadVisible Visibility of the mark message as unread option.
 * @param isCopyTextVisible Visibility of the copy message text option.
 * @param isEditMessageVisible Visibility of the edit message option.
 * @param isFlagMessageVisible Visibility of the flag message option.
 * @param isPinMessageVisible Visibility of the pin message to chat option.
 * @param isDeleteMessageVisible Visibility of the delete message option.
 * @param isMuteUserVisible Visibility of the mute user option.
 * @param isBlockUserVisible Visibility of the block user option.
 *
 * @see [SelectedMessageMenu]
 * @see [MessageOptions]
 * @see [defaultMessageOptionsState]
 */
public data class MessageOptionItemVisibility(
    val isRetryMessageVisible: Boolean = true,
    val isReplyVisible: Boolean = true,
    val isThreadReplyVisible: Boolean = true,
    val isMarkAsUnreadVisible: Boolean = true,
    val isCopyTextVisible: Boolean = true,
    val isEditMessageVisible: Boolean = true,
    val isFlagMessageVisible: Boolean = true,
    val isPinMessageVisible: Boolean = true,
    val isDeleteMessageVisible: Boolean = true,
    val isMuteUserVisible: Boolean = true,
    val isBlockUserVisible: Boolean = true,
)
