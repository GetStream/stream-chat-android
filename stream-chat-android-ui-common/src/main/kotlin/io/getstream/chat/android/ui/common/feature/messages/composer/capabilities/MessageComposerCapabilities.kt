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

package io.getstream.chat.android.ui.common.feature.messages.composer.capabilities

import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState

/**
 * Determines whether the user can send a message in the current state.
 *
 * This function checks both channel-level capabilities and user preferences to determine
 * if a message can be sent. The required capability depends on the current message mode:
 * - In thread mode: requires [ChannelCapabilities.SEND_REPLY] capability
 * - In regular mode: requires [ChannelCapabilities.SEND_MESSAGE] capability
 *
 * The final decision also considers the [MessageComposerState.sendEnabled] flag, which allows
 * temporary disabling of sending (e.g., while uploading attachments or validating input).
 *
 * @return `true` if the user can send a message or reply based on both capabilities and
 * the send enabled flag; `false` otherwise.
 */
public fun MessageComposerState.canSendMessage(): Boolean {
    val canSendMessage = ownCapabilities.contains(ChannelCapabilities.SEND_MESSAGE)
    val canSendReply = ownCapabilities.contains(ChannelCapabilities.SEND_REPLY)
    val isInThread = messageMode is MessageMode.MessageThread
    val canSend = if (isInThread) {
        canSendReply
    } else {
        canSendMessage
    }
    // The final send capability depends on the channel capabilities, and potentially the user-set sendEnabled flag
    return sendEnabled && canSend
}

/**
 * Determines whether the user can upload files in the current state.
 *
 * This function checks if the user has the [ChannelCapabilities.UPLOAD_FILE] capability
 * for the current channel. This capability allows users to attach and upload files
 * (documents, images, videos, etc.) through the message composer.
 *
 * @return `true` if the user has the [ChannelCapabilities.UPLOAD_FILE] capability; `false` otherwise.
 */
public fun MessageComposerState.canUploadFile(): Boolean = ownCapabilities.contains(ChannelCapabilities.UPLOAD_FILE)
