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

package io.getstream.chat.android.ui.message.list.reactions.view.internal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.message.list.reactions.view.ViewReactionsViewStyle
import io.getstream.chat.android.ui.utils.extensions.isRtlLayout

/**
 * Draw the bubble of view reactions.
 */
internal class ViewReactionsBubbleDrawer(
    private val viewReactionsViewStyle: ViewReactionsViewStyle,
) {

    private val bubblePaintTheirs = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = viewReactionsViewStyle.bubbleColorTheirs
        style = Paint.Style.FILL
    }
    private val bubblePaintMine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = viewReactionsViewStyle.bubbleColorMine
        style = Paint.Style.FILL
    }
    private val bubbleStrokePaintMine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = viewReactionsViewStyle.bubbleBorderColorMine
        strokeWidth = viewReactionsViewStyle.bubbleBorderWidthMine
        style = Paint.Style.STROKE
    }

    private val bubbleStrokePaintTheirs by lazy {
        check(shouldDrawTheirsBorder()) {
            "You need to specify either bubbleBorderColorTheirs and bubbleBorderWidthTheirs to draw a border for another user reaction bubble"
        }
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = viewReactionsViewStyle.bubbleBorderColorTheirs!!
            strokeWidth = viewReactionsViewStyle.bubbleBorderWidthTheirs!!
            style = Paint.Style.STROKE
        }
    }

    private var bubbleWidth: Int = 0
    private var isMyMessage: Boolean = false
    private var isSingleReaction: Boolean = false

    /**
     * Draws the bubble of reactions choosing the correct direction.
     *
     * @param context [Context].
     * @param canvas [Canvas].
     * @param bubbleWidth The width of the bubble. This should be at least bigger than all the columns of reactions.
     * @param isMyMessage Whether this is the message of the current user or not.
     * @param isSingleReaction Whether there's only a single reaction of there are multiple reactions.
     * @param inverseBubbleStyle Used to invert the side of the bubble.
     */
    fun drawReactionsBubble(
        context: Context,
        canvas: Canvas,
        bubbleWidth: Int,
        isMyMessage: Boolean,
        isSingleReaction: Boolean,
        inverseBubbleStyle: Boolean = false,
    ) {
        this.isMyMessage = isMyMessage
        this.bubbleWidth = bubbleWidth
        this.isSingleReaction = isSingleReaction

        val isRtl = context.isRtlLayout

        val path = Path().apply {
            op(createBubbleRoundRectPath(), Path.Op.UNION)
            op(createLargeTailBubblePath(isRtl), Path.Op.UNION)
            op(createSmallTailBubblePath(isRtl), Path.Op.UNION)
        }

        val outlineStyle = if (inverseBubbleStyle) !isMyMessage else isMyMessage
        if (outlineStyle) {
            canvas.drawPath(path, bubblePaintMine)
            canvas.drawPath(path, bubbleStrokePaintMine)
        } else {
            canvas.drawPath(path, bubblePaintTheirs)
            if (shouldDrawTheirsBorder()) {
                canvas.drawPath(path, bubbleStrokePaintTheirs)
            }
        }
    }

    private fun shouldDrawTheirsBorder(): Boolean =
        viewReactionsViewStyle.bubbleBorderColorTheirs != null && viewReactionsViewStyle.bubbleBorderWidthTheirs != null

    private fun createBubbleRoundRectPath(): Path {
        val strokeOffset = getStrokeOffset()
        return Path().apply {
            addRoundRect(
                strokeOffset,
                strokeOffset,
                bubbleWidth.toFloat() - strokeOffset,
                viewReactionsViewStyle.bubbleHeight.toFloat(),
                viewReactionsViewStyle.bubbleRadius.toFloat(),
                viewReactionsViewStyle.bubbleRadius.toFloat(),
                Path.Direction.CW
            )
        }
    }

    private fun getStrokeOffset(): Float {
        return when {
            isMyMessage -> {
                viewReactionsViewStyle.bubbleBorderWidthMine / 2
            }
            shouldDrawTheirsBorder() -> {
                viewReactionsViewStyle.bubbleBorderWidthTheirs!! / 2
            }
            else -> {
                0f
            }
        }
    }

    /**
     * Draws the path of large tail bubble.
     *
     * @param isRtl If the bubble should be drawn with inverted direction.
     */
    private fun createLargeTailBubblePath(isRtl: Boolean): Path {
        return Path().apply {
            addCircle(
                positionBubble(isRtl, viewReactionsViewStyle.largeTailBubbleOffset.toFloat()),
                viewReactionsViewStyle.largeTailBubbleCy.toFloat(),
                viewReactionsViewStyle.largeTailBubbleRadius.toFloat(),
                Path.Direction.CW
            )
        }
    }

    /**
     * Draws the path of small tail bubble.
     *
     * @param isRtl If the bubble should be drawn with inverted direction.
     */
    private fun createSmallTailBubblePath(isRtl: Boolean): Path {
        return Path().apply {
            addCircle(
                positionBubble(isRtl, viewReactionsViewStyle.smallTailBubbleOffset.toFloat()),
                viewReactionsViewStyle.smallTailBubbleCy.toFloat(),
                viewReactionsViewStyle.smallTailBubbleRadius.toFloat() - getStrokeOffset(),
                Path.Direction.CW
            )
        }
    }

    private fun calculateBubbleCenterX(bubbleOffset: Float): Float {
        return if (isMyMessage) {
            if (isSingleReaction) {
                bubbleWidth / 2 + bubbleOffset
            } else {
                bubbleWidth + bubbleOffset - MULTIPLE_REACTIONS_BASELINE_OFFSET
            }
        } else {
            if (isSingleReaction) {
                bubbleWidth / 2 - bubbleOffset
            } else {
                MULTIPLE_REACTIONS_BASELINE_OFFSET - bubbleOffset
            }
        }
    }

    /**
     * Applies an offset in the bubble, respecting RTL support.
     *
     * @param isRtl If the bubble should be drawn with inverted direction.
     * @param bubbleOffset The offset to apply.
     */
    private fun positionBubble(isRtl: Boolean, bubbleOffset: Float): Float {
        return calculateBubbleCenterX(bubbleOffset).let { offset ->
            if (isRtl) {
                bubbleWidth.toFloat() - offset
            } else {
                offset
            }
        }
    }

    private companion object {
        private val MULTIPLE_REACTIONS_BASELINE_OFFSET = 32.dpToPx()
    }
}
