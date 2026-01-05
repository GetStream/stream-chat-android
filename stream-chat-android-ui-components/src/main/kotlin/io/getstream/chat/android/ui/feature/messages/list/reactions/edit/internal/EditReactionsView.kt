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

package io.getstream.chat.android.ui.feature.messages.list.reactions.edit.internal

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.feature.messages.list.reactions.ReactionClickListener
import io.getstream.chat.android.ui.feature.messages.list.reactions.edit.EditReactionsViewStyle
import io.getstream.chat.android.ui.feature.messages.list.reactions.internal.ReactionItem
import io.getstream.chat.android.ui.feature.messages.list.reactions.internal.ReactionsAdapter
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import kotlin.math.ceil

private const val TAIL_BUBBLE_SPACE_DP = 16

@InternalStreamChatApi
public class EditReactionsView : RecyclerView {

    private lateinit var reactionsViewStyle: EditReactionsViewStyle
    private lateinit var reactionsAdapter: ReactionsAdapter
    private lateinit var bubbleDrawer: EditReactionsBubbleDrawer

    private var reactionClickListener: ReactionClickListener? = null
    private var isMyMessage: Boolean = false
    private var messageAnchorPosition: Float = 0f

    public constructor(context: Context) : super(context.createStreamThemeWrapper()) {
        init(context, null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs) {
        init(context, attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
    ) {
        init(context, attrs)
    }

    private var bubbleHeight: Int = 0
    private var reactionsColumns: Int = 5

    public fun setMessage(message: Message, isMyMessage: Boolean) {
        this.isMyMessage = isMyMessage

        val reactionItems: List<ReactionItem> = ChatUI.supportedReactions.reactions.map { (type, reactionDrawable) ->
            ReactionItem(
                type = type,
                isMine = message.ownReactions.any { it.type == type },
                reactionDrawable = reactionDrawable,
            )
        }

        if (reactionItems.size > reactionsColumns) {
            val timesBigger = ceil(reactionItems.size.toFloat() / reactionsColumns).toInt()
            bubbleHeight = bubbleHeight.times(timesBigger)
        }

        minimumHeight = bubbleHeight + TAIL_BUBBLE_SPACE_DP.dpToPx()

        reactionsAdapter.submitList(reactionItems)
    }

    public fun setReactionClickListener(reactionClickListener: ReactionClickListener) {
        this.reactionClickListener = reactionClickListener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val canvasWidth = width
        val bubbleDrawOffset = 12.dpToPx()
        val boundsEnd = canvasWidth - bubbleDrawOffset

        val canvasBounds = bubbleDrawOffset..boundsEnd

        bubbleDrawer.drawReactionsBubble(
            context = context,
            canvas = canvas,
            bubbleWidth = width,
            bubbleHeight = bubbleHeight,
            isMyMessage = isMyMessage,
            isSingleReaction = true,
            messageAnchorPosition = messageAnchorPosition,
            canvasBounds = canvasBounds,
        )
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val style = EditReactionsViewStyle(context, attrs)
        applyStyle(style)

        bubbleHeight = style.bubbleHeight

        itemAnimator = null
        overScrollMode = View.OVER_SCROLL_NEVER
        setWillNotDraw(false)
    }

    internal fun applyStyle(editReactionsViewStyle: EditReactionsViewStyle) {
        this.reactionsViewStyle = editReactionsViewStyle
        this.bubbleDrawer = EditReactionsBubbleDrawer(reactionsViewStyle)

        reactionsColumns = minOf(ChatUI.supportedReactions.reactions.size, editReactionsViewStyle.reactionsColumn)
        setPadding(
            reactionsViewStyle.horizontalPadding,
            reactionsViewStyle.verticalPadding,
            reactionsViewStyle.horizontalPadding,
            reactionsViewStyle.verticalPadding,
        )

        layoutManager = GridLayoutManager(context, reactionsColumns)

        adapter = ReactionsAdapter(reactionsViewStyle.itemSize) {
            reactionClickListener?.onReactionClick(it)
        }.also { reactionsAdapter = it }
    }

    internal fun positionBubbleTail(messageAnchorPosition: Float) {
        this.messageAnchorPosition = messageAnchorPosition

        requestLayout()
    }
}
