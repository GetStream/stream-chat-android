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

package io.getstream.chat.android.compose.ui.util

import io.getstream.chat.android.compose.state.messages.MessageAlignment
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState

/**
 *  An interface that allows to return the desired horizontal alignment for a particular [MessageItemState].
 */
public fun interface MessageAlignmentProvider {

    /**
     * Returns [MessageAlignment] for a particular [MessageItemState].
     *
     * @param messageItem The message whose data is used to decide which alignment to use.
     * @return The [MessageAlignment] for the provided message.
     */
    public fun provideMessageAlignment(messageItem: MessageItemState): MessageAlignment

    public companion object {
        /**
         * Builds the default message alignment provider.
         *
         * @see [DefaultMessageAlignmentProvider]
         */
        public fun defaultMessageAlignmentProvider(): MessageAlignmentProvider = DefaultMessageAlignmentProvider()
    }
}

/**
 * A simple implementation of [MessageAlignmentProvider] that returns [MessageAlignment.End]
 * for the messages of the current user and [MessageAlignment.Start] for the messages of
 * other users.
 */
private class DefaultMessageAlignmentProvider : MessageAlignmentProvider {

    /**
     * Returns [MessageAlignment] for a particular [MessageItemState].
     *
     * @param messageItem The message whose data is used to decide which alignment to use.
     * @return The [MessageAlignment] for the provided message.
     */
    override fun provideMessageAlignment(messageItem: MessageItemState): MessageAlignment = if (messageItem.isMine) MessageAlignment.End else MessageAlignment.Start
}
