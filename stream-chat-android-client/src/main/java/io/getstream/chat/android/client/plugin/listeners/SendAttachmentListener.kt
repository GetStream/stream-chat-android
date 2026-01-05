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

package io.getstream.chat.android.client.plugin.listeners

import io.getstream.chat.android.models.Message

/**
 * Listener that updates the SDK accordingly with the request to send attachments to the backend.
 */
public interface SendAttachmentListener {

    /**
     * Updates the SDK before the attachments are sent to backend. It can be used to update the
     * database with the message whose attachments are going to be sent or to change the state
     * of the messages that are presented to the end user.
     *
     * @param channelType String
     * @param channelId String
     * @param message [Message]
     */
    public suspend fun onAttachmentSendRequest(
        channelType: String,
        channelId: String,
        message: Message,
    )
}
