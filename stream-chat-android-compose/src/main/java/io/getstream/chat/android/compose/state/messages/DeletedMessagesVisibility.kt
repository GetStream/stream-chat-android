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
 
package io.getstream.chat.android.compose.state.messages

/**
 * Represents the behavior of deleted messages in the list, if they should show for some users or no one.
 */
public enum class DeletedMessagesVisibility {
    /**
     * No deleted messages are visible.
     */
    NONE,

    /**
     * Only the deleted messages that the current user owns are visible.
     */
    OWN,

    /**
     * All deleted messages are visible, regardless of the owner.
     */
    ALL
}
