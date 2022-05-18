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

package io.getstream.chat.android.ui.message.list.reactions.view

import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Determines the orientation of the reaction bubble inside message options user reactions.
 *
 * @param value The int value of the enum used for xml attributes.
 */
public enum class MessageOptionsUserReactionBubbleOrientation(public val value: Int) {
    /**
     * Aligns the reaction to the start of the user avatar.
     */
    START(0),

    /**
     * Aligns the reaction to the end of user avatar.
     */
    END(1),

    /**
     * Aligns the reaction to the end of user avatar if the reaction is from the current user otherwise aligns it
     * to the start of the avatar.
     */
    BY_USER(2),

    /**
     *
     */
    BY_USER_INVERTED(3)
}

public fun Int.getUserReactionOrientation(): MessageOptionsUserReactionBubbleOrientation {
    return MessageOptionsUserReactionBubbleOrientation.values().firstOrNull { it.value == this }
        ?: error("No such alignment")
}

/**
 * Determines if the user reaction should be aligned to start or end.
 *
 * @param isMine Is the reaction the current users reaction.
 *
 * @return If the reaction is aligned to the start or not.
 */
@InternalStreamChatApi
public fun MessageOptionsUserReactionBubbleOrientation.isOrientedTowardsStart(isMine: Boolean): Boolean {
    return this == MessageOptionsUserReactionBubbleOrientation.START ||
        (isMine && this == MessageOptionsUserReactionBubbleOrientation.BY_USER) ||
        (!isMine && this == MessageOptionsUserReactionBubbleOrientation.BY_USER_INVERTED)
}
