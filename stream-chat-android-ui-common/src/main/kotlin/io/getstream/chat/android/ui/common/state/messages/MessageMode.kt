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

package io.getstream.chat.android.ui.common.state.messages

import io.getstream.chat.android.client.api.state.ThreadState
import io.getstream.chat.android.models.Message

/**
 * Represents the message mode that's currently active.
 */
public sealed class MessageMode {

    /**
     * Regular mode, conversation with other users.
     */
    public data object Normal : MessageMode()

    /**
     * Thread mode, where there's a parent message to respond to.
     *
     * @param parentMessage The message users are responding to in a Thread.
     * @param threadState The state of the current thread.
     */
    public data class MessageThread @JvmOverloads constructor(
        public val parentMessage: Message,
        public val threadState: ThreadState? = null,
    ) : MessageMode()
}

internal fun MessageMode.stringify(): String = when (this) {
    MessageMode.Normal -> "Normal"
    is MessageMode.MessageThread -> "MessageThread(parentMessage.id=${parentMessage.id})"
}
