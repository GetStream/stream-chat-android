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

package io.getstream.chat.android.previewdata

import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User

/**
 * Provides sample pinned messages data that will be used to render component previews.
 */
public object PreviewPinnedMessageData {
    /**
     * Sample pinned message.
     */
    public val pinnedMessage1: Message =
        Message(
            id = "msg1",
            cid = "messaging:123",
            text = "Some very long pinned message in the chat from a while ago.",
            user =
            User(
                id = "usr1",
                name = "Test User",
            ),
        )

    /**
     * Sample pinned message.
     */
    public val pinnedMessage2: Message =
        Message(
            id = "msg1",
            cid = "messaging:123",
            text = "Important message pinned to the chat.",
            user =
            User(
                id = "usr2",
                name = "Sample User",
            ),
        )

    /**
     * List of pinned messages.
     */
    public val pinnedMessageList: List<Message> =
        listOf(
            pinnedMessage1,
            pinnedMessage2,
        )

    /**
     * List of pinned messages + loading more indicator.
     */
    public val pinnedMessageListWithLoadingMore: List<Message> =
        listOf(
            pinnedMessage1,
            pinnedMessage2,
            Message(), // used as loading more indicator
        )
}
