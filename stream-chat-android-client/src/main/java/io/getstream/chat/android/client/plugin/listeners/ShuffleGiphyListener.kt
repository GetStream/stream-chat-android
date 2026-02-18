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

package io.getstream.chat.android.client.plugin.listeners

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Message
import io.getstream.result.Result

/**
 * Listener for [io.getstream.chat.android.client.ChatClient.shuffleGiphy] calls.
 */
@InternalStreamChatApi
public interface ShuffleGiphyListener {

    /**
     * A method called after receiving the response from the shuffle Giphy call.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param result The API call result.
     */
    public suspend fun onShuffleGiphyResult(
        cid: String,
        result: Result<Message>,
    )
}
