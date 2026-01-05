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

package io.getstream.chat.android.ui.common.state.messages.list

/**
 * Intended to be used for regulating visibility of deleted messages
 * and filtering them out accordingly.
 */
public enum class DeletedMessageVisibility {

    /**
     * No deleted messages are visible.
     */
    ALWAYS_HIDDEN,

    /**
     * Deleted messages from the current user are visible,
     * ones from other users are not.
     */
    VISIBLE_FOR_CURRENT_USER,

    /**
     * All deleted messages are visible, regardless of the
     * user who authored them.
     */
    ALWAYS_VISIBLE,
}
