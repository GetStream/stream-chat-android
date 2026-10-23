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

package io.getstream.chat.android.client.extensions.internal

import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.NotificationChannelTruncatedEvent
import io.getstream.chat.android.models.Message
import java.util.Date

/**
 * The date before which the truncation removed messages. The truncation's system message is created just after
 * it, while the event itself is created later.
 */
internal val ChannelTruncatedEvent.truncatedBefore: Date
    get() = channel.truncatedAt ?: createdAt

/**
 * The date before which the truncation removed messages.
 */
internal val NotificationChannelTruncatedEvent.truncatedBefore: Date
    get() = channel.truncatedAt ?: createdAt

/**
 * Whether the message outlives a truncation at [truncatedAt]. Compares the server's creation date, as the backend
 * does, because the local one follows the device clock; a message the server has not created yet is kept.
 */
internal fun Message.survivesTruncationAt(truncatedAt: Date): Boolean = createdAt?.before(truncatedAt) != true
