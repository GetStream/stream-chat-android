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

package io.getstream.chat.android.ui.widgets.avatar

/**
 * Determines the position of online indicator in [UserAvatarView] and [ChannelAvatarView].
 */
public enum class OnlineIndicatorPosition {

    /**
     * The indicator is positioned at the top-start of the avatar.
     */
    TOP_START,

    /**
     * The indicator is positioned at the top-end of the avatar.
     */
    TOP_END,

    /**
     * The indicator is positioned at the bottom-start of the avatar.
     */
    BOTTOM_START,

    /**
     * The indicator is positioned at the bottom-end of the avatar.
     */
    BOTTOM_END,
}
