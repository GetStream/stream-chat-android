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

package io.getstream.chat.android.compose.ui.chats

import io.getstream.chat.android.core.ExperimentalStreamChatApi
import java.io.Serializable

/**
 * Represents the selection of a message within a channel.
 *
 * Implements [Serializable] to allow for saving and restoring the state across configuration changes.
 */
@ExperimentalStreamChatApi
@Suppress("SerialVersionUIDInSerializableClass")
public data class ChatMessageSelection(
    /**
     * The ID of the selected channel, or `null` if no channel is selected.
     */
    val channelId: String? = null,
    /**
     * The ID of a specific message, or `null` if navigating to a channel without a pre-selected message.
     */
    val messageId: String? = null,
    /**
     * The ID of the parent message (for threads), or `null` if not in a thread.
     */
    val parentMessageId: String? = null,
) : Serializable
