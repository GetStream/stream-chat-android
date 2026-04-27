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

package io.getstream.chat.android.client.api.state

import io.getstream.chat.android.models.Message
import kotlinx.coroutines.flow.StateFlow

/** State container with reactive data of a thread.*/
public interface ThreadState {

    /** The message id for the parent of this thread. */
    public val parentId: String

    /** The sorted list of messages for this thread. */
    public val messages: StateFlow<List<Message>>

    /** If we are currently loading messages. */
    public val loading: StateFlow<Boolean>

    /** If we've reached the earliest point in this thread. */
    public val endOfOlderMessages: StateFlow<Boolean>

    /** If we've reached the latest point in this thread. */
    public val endOfNewerMessages: StateFlow<Boolean>

    /** The oldest message available in this thread state.
     * It's null when we haven't loaded any messages in thread yet. */
    public val oldestInThread: StateFlow<Message?>

    /** The newest message available in this thread state.
     * It's null when we haven't loaded any messages in thread yet. */
    public val newestInThread: StateFlow<Message?>
}
