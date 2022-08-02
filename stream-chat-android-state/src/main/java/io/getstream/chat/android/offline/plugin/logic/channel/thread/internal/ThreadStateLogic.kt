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

package io.getstream.chat.android.offline.plugin.logic.channel.thread.internal

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.offline.plugin.state.channel.thread.internal.ThreadMutableState

/**
 * The logic of the state of a thread. This class contains the logic of how to
 * update the state of the thread in the SDK.
 */
internal interface ThreadStateLogic {

    /**
     * Return [ThreadMutableState] representing the state of the thread. Use this when you would like to
     * keep track of the state and would like to write a new state too.
     */
    fun writeThreadState(): ThreadMutableState

    /**
     * Deletes a message for the thread
     *
     * @param message [Message]
     */
    fun deleteMessage(message: Message)

    /**
     * Upsert message in the thread.
     *
     * @param message The message to be added or updated.
     */
    fun upsertMessage(message: Message)

    /**
     * Upsert messages in the channel.
     *
     * @param messages the list of [Message] to be upserted
     * new messages should be kept.
     */
    fun upsertMessages(messages: List<Message>)

    /**
     * Removes local message. Doesn't remove message in database.
     *
     * @param message The [Message] to be deleted.
     */
    fun removeLocalMessage(message: Message)
}
