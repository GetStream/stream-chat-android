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

package io.getstream.chat.android.ui.feature.messages.list.reactions.view

import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Determines the orientation of the reaction bubble inside message options user reactions.
 *
 * @param value The int value of the enum used for xml attributes.
 */
public enum class MessageOptionsUserReactionOrientation(public val value: Int) {
    /**
     * Orients the reaction to the start of the user avatar.
     */
    START(0),

    /**
     * Orients the reaction to the end of user avatar.
     */
    END(1),

    /**
     * Orients the reaction to the end of user avatar if the reaction is from the current user otherwise orients it
     * to the start of the avatar.
     */
    BY_USER(2),

    /**
     * Orients the reaction to the start of user avatar if the reaction is from the current user otherwise orients it
     * to the end of the avatar.
     */
    BY_USER_INVERTED(3),
}

/**
 * @return The corresponding [MessageOptionsUserReactionOrientation] for the passed attribute in xml.
 */
public fun Int.getUserReactionOrientation(): MessageOptionsUserReactionOrientation {
    return MessageOptionsUserReactionOrientation.values().firstOrNull { it.value == this }
        ?: error("No such alignment")
}

/**
 * Determines if the user reaction should be oriented to start or end.
 *
 * @param isMine Is the reaction the current users reaction.
 *
 * @return If the reaction is oriented towards start or not.
 */
@InternalStreamChatApi
public fun MessageOptionsUserReactionOrientation.isOrientedTowardsStart(isMine: Boolean): Boolean {
    return this == MessageOptionsUserReactionOrientation.START ||
        (isMine && this == MessageOptionsUserReactionOrientation.BY_USER) ||
        (!isMine && this == MessageOptionsUserReactionOrientation.BY_USER_INVERTED)
}
