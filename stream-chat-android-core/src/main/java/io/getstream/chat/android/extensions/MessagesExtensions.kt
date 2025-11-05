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

package io.getstream.chat.android.extensions

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageType
import java.util.Date

/**
 * Returns the date of the last message in the list, excluding shadowed messages and system messages.
 *
 * @param skipLastMsgUpdateForSystemMsgs If true, system ./messages will be excluded from the last message calculation.
 * @return The date of the last message or null if there are no valid messages.
 */
@InternalStreamChatApi
public fun List<Message>.lastMessageAt(skipLastMsgUpdateForSystemMsgs: Boolean): Date? = this
    .filterNot { it.shadowed }
    .filterNot { it.parentId != null && !it.showInChannel }
    .filterNot { it.type == MessageType.SYSTEM && skipLastMsgUpdateForSystemMsgs }
    .maxByOrNull { it.createdLocallyAt ?: it.createdAt ?: Date(0) }
    ?.let { it.createdLocallyAt ?: it.createdAt }
