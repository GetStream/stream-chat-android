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

package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.experimental.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry

internal class ThreadQueryListenerImpl(private val logic: LogicRegistry) : ThreadQueryListener {

    override fun onGetRepliesPrecondition(messageId: String, limit: Int): Result<Unit> =
        logic.thread(messageId).onGetRepliesPrecondition(messageId, limit)

    override fun onGetRepliesRequest(messageId: String, limit: Int): Unit =
        logic.thread(messageId).onGetRepliesRequest(messageId, limit)

    override fun onGetRepliesResult(result: Result<List<Message>>, messageId: String, limit: Int): Unit =
        logic.thread(messageId).onGetRepliesResult(result, messageId, limit)

    override fun onGetRepliesMorePrecondition(messageId: String, firstId: String, limit: Int): Result<Unit> =
        logic.thread(messageId).onGetRepliesMorePrecondition(messageId, firstId, limit)

    override fun onGetRepliesMoreRequest(messageId: String, firstId: String, limit: Int): Unit =
        logic.thread(messageId).onGetRepliesMoreRequest(messageId, firstId, limit)

    override fun onGetRepliesMoreResult(
        result: Result<List<Message>>,
        messageId: String,
        firstId: String,
        limit: Int,
    ): Unit = logic.thread(messageId).onGetRepliesMoreResult(result, messageId, firstId, limit)
}
