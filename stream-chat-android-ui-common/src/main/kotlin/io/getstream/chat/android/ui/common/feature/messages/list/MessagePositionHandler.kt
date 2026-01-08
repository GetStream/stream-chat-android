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

package io.getstream.chat.android.ui.common.feature.messages.list

import io.getstream.chat.android.client.utils.message.isError
import io.getstream.chat.android.client.utils.message.isSystem
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition

/**
 * A handler to determine the position of a message inside a group.
 */
public fun interface MessagePositionHandler {
    /**
     * Determines the position of a message inside a group.
     *
     * @param previousMessage The previous [Message] in the list.
     * @param message The current [Message] in the list.
     * @param nextMessage The next [Message] in the list.
     * @param isAfterDateSeparator If a date separator was added before the current [Message].
     * @param isBeforeDateSeparator If a date separator was added after the current [Message].
     * @param isInThread If the current [Message] is in a thread.
     *
     * @return The position of the current message inside the group.
     */
    public fun handleMessagePosition(
        previousMessage: Message?,
        message: Message,
        nextMessage: Message?,
        isAfterDateSeparator: Boolean,
        isBeforeDateSeparator: Boolean,
        isInThread: Boolean,
    ): List<MessagePosition>

    public companion object {
        /**
         * The default implementation of the [MessagePositionHandler] interface which can be taken
         * as a reference when implementing a custom one.
         *
         * @return The default implementation of [MessagePositionHandler].
         */
        @InternalStreamChatApi
        @Suppress("ComplexCondition")
        public fun defaultHandler(): MessagePositionHandler {
            return MessagePositionHandler {
                    prevMessage: Message?,
                    curMessage: Message,
                    nextMessage: Message?,
                    isAfterDateSeparator: Boolean,
                    isBeforeDateSeparator: Boolean,
                    _: Boolean,
                ->

                val curUser = curMessage.user
                mutableListOf<MessagePosition>().apply {
                    val isTop = !isBeforeDateSeparator &&
                        (isAfterDateSeparator || prevMessage.isInvalidMessageFromUser(curUser)) &&
                        nextMessage.isValidMessageFromUser(curUser)

                    val isMiddle = !isAfterDateSeparator && !isBeforeDateSeparator &&
                        prevMessage.isValidMessageFromUser(curUser) &&
                        nextMessage.isValidMessageFromUser(curUser)

                    val isBottom = !isAfterDateSeparator &&
                        (isBeforeDateSeparator || nextMessage.isInvalidMessageFromUser(curUser)) &&
                        prevMessage.isValidMessageFromUser(curUser)

                    if (isTop) add(MessagePosition.TOP)
                    if (isMiddle) add(MessagePosition.MIDDLE)
                    if (isBottom) add(MessagePosition.BOTTOM)
                    if (isEmpty()) add(MessagePosition.NONE)
                }
            }
        }

        private fun Message?.isValidMessageFromUser(user: User): Boolean {
            return this != null && !this.isSystem() && !this.isError() && this.user.id == user.id
        }

        private fun Message?.isInvalidMessageFromUser(user: User): Boolean {
            return isValidMessageFromUser(user).not()
        }
    }
}
