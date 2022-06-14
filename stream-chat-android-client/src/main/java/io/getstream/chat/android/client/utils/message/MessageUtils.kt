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

@file:JvmName("MessageUtils")

package io.getstream.chat.android.client.utils.message

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.utils.date.after

private const val ITEM_COUNT_OF_TWO: Int = 2

/**
 * Peeks the latest message from the sorted [List] of messages.
 */
@InternalStreamChatApi
public fun List<Message>.latestOrNull(): Message? = when (size >= ITEM_COUNT_OF_TWO) {
    true -> {
        val first = first()
        val last = last()
        when (last.createdAfter(first)) {
            true -> last
            else -> first
        }
    }
    else -> lastOrNull()
}

/**
 * Tests if [this] message was created after [that] message.
 */
@InternalStreamChatApi
public fun Message.createdAfter(that: Message): Boolean {
    val thisDate = this.createdAt ?: this.createdLocallyAt
    val thatDate = that.createdAt ?: that.createdLocallyAt
    return thisDate after thatDate
}
