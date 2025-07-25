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

package io.getstream.chat.android.client.extensions.internal

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.MessageReminder
import io.getstream.chat.android.models.MessageReminderInfo

/**
 * Converts a [MessageReminder] to a [MessageReminderInfo].
 *
 * @return The converted [MessageReminderInfo].
 */
@InternalStreamChatApi
public fun MessageReminder.toMessageReminderInfo(): MessageReminderInfo = MessageReminderInfo(
    remindAt = remindAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
