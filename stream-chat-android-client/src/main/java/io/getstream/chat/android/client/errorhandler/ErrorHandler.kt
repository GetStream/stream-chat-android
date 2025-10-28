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

package io.getstream.chat.android.client.errorhandler

import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Extension for [io.getstream.chat.android.client.ChatClient] that allows handling plugins' errors.
 *
 * @see [io.getstream.chat.android.client.experimental.plugin.Plugin]
 */
@InternalStreamChatApi
public interface ErrorHandler :
    DeleteReactionErrorHandler,
    CreateChannelErrorHandler,
    QueryMembersErrorHandler,
    SendReactionErrorHandler,
    Comparable<ErrorHandler> {

    /**
     * The priority of this [ErrorHandler]. Use it to run it before error handlers of the same type.
     */
    public val priority: Int

    override fun compareTo(other: ErrorHandler): Int = this.priority.compareTo(other.priority)

    public companion object {

        /**
         * Default priority
         */
        public const val DEFAULT_PRIORITY: Int = 1
    }
}
