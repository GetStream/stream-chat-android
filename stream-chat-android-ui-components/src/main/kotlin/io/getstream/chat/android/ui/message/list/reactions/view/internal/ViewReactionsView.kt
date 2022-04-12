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
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.common.extensions.hasSingleReaction
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.supportedReactionCounts
import io.getstream.chat.android.ui.message.list.reactions.ReactionClickListener
import io.getstream.chat.android.ui.message.list.reactions.internal.ReactionItem
import io.getstream.chat.android.ui.message.list.reactions.internal.ReactionsAdapter
import io.getstream.chat.android.ui.message.list.reactions.view.ViewReactionsViewStyle

@InternalStreamChatApi
public class ViewReactionsView : RecyclerView {

    private lateinit var reactionsViewStyle: ViewReactionsViewStyle
    private lateinit var reactionsAdapter: ReactionsAdapter
    private lateinit var bubbleDrawer: ViewReactionsBubbleDrawer

    private var reactionClickListener: ReactionClickListener? = null
    private var isMyMessage: Boolean = false
    private var isSingleReaction: Boolean = true

    public constructor(context: Context) : super(context.createStreamThemeWrapper()) {
        init(context, null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs) {
        init(context, attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    public fun setMessage(message: Message, isMyMessage: Boolean, commitCallback: () -> Unit = {}) {
        this.isMyMessage = isMyMessage
        this.isSingleReaction = message.hasSingleReaction()

        reactionsAdapter.submitList(createReactionItems(message)) {
            val horizontalPadding = if (isSingleReaction) 0 else reactionsViewStyle.horizontalPadding
            setPadding(horizontalPadding, 0, horizontalPadding, 0)

            commitCallback()
        }
    }

    public fun setReactionClickListener(reactionClickListener: ReactionClickListener) {
        this.reactionClickListener = reactionClickListener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bubbleDrawer.drawReactionsBubble(context, canvas, width, isMyMessage, isSingleReaction)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        applyStyle(ViewReactionsViewStyle(context, attrs))
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        itemAnimator = null
        overScrollMode = View.OVER_SCROLL_NEVER
        setWillNotDraw(false)
    }

    internal fun applyStyle(style: ViewReactionsViewStyle) {
        this.reactionsViewStyle = style
        this.bubbleDrawer = ViewReactionsBubbleDrawer(reactionsViewStyle)
        minimumHeight = reactionsViewStyle.totalHeight
        reactionsViewStyle.horizontalPadding.let {
            setPadding(it, 0, it, 0)
        }

        adapter = ReactionsAdapter(reactionsViewStyle.itemSize) {
            reactionClickListener?.onReactionClick(it)
        }.also { reactionsAdapter = it }
    }

    private fun createReactionItems(message: Message): List<ReactionItem> {
        return message.supportedReactionCounts.keys
            .mapNotNull { type ->
                ChatUI.supportedReactions.getReactionDrawable(type)?.let {
                    ReactionItem(
                        type = type,
                        isMine = message.ownReactions.any { it.type == type },
                        reactionDrawable = it
                    )
                }
            }.sortedBy { if (isMyMessage) it.isMine else !it.isMine }
    }
}
