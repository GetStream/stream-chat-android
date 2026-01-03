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

package io.getstream.chat.android.ui.utils.extensions

import io.getstream.chat.android.ui.feature.messages.list.reactions.user.SingleReactionViewStyle
import io.getstream.chat.android.ui.feature.messages.list.reactions.view.ViewReactionsViewStyle

/**
 * Converts [ViewReactionsViewStyle] to [SingleReactionViewStyle].
 *
 * @return [SingleReactionViewStyle] from [ViewReactionsViewStyle].
 */
internal fun ViewReactionsViewStyle.toSingleReactionViewStyle(): SingleReactionViewStyle {
    return SingleReactionViewStyle(
        bubbleBorderColorMine = bubbleBorderColorMine,
        bubbleBorderColorTheirs = bubbleBorderColorTheirs,
        bubbleColorMine = bubbleColorMine,
        bubbleColorTheirs = bubbleColorTheirs,
        bubbleBorderWidthMine = bubbleBorderWidthMine,
        bubbleBorderWidthTheirs = bubbleBorderWidthTheirs,
        totalHeight = totalHeight,
        bubbleHeight = bubbleHeight,
        bubbleRadius = bubbleRadius,
        largeTailBubbleCy = largeTailBubbleCy,
        largeTailBubbleRadius = largeTailBubbleRadius,
        largeTailBubbleOffset = largeTailBubbleOffset,
        smallTailBubbleCy = smallTailBubbleCy,
        smallTailBubbleRadius = smallTailBubbleRadius,
        smallTailBubbleOffset = smallTailBubbleOffset,
        reactionOrientation = messageOptionsUserReactionOrientation,
    )
}
