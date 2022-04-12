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

package io.getstream.chat.android.ui.message.list.reactions.user.internal

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageReactionBinding
import io.getstream.chat.android.ui.message.list.reactions.view.ViewReactionsViewStyle
import io.getstream.chat.android.ui.message.list.reactions.view.internal.ViewReactionsBubbleDrawer

internal class SingleReactionView : FrameLayout {
    private val binding = StreamUiItemMessageReactionBinding.inflate(streamThemeInflater, this, true)
    private lateinit var reactionsViewStyle: ViewReactionsViewStyle
    private lateinit var bubbleDrawer: ViewReactionsBubbleDrawer
    private var isMyMessage: Boolean = false

    constructor(context: Context) : super(context.createStreamThemeWrapper()) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    fun setReaction(userReactionItem: UserReactionItem) {
        // according to the design, current user reactions have the same style
        // as reactions on the current user messages in the message list
        this.isMyMessage = !userReactionItem.isMine
        binding.reactionIcon.setImageDrawable(userReactionItem.drawable)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bubbleDrawer.drawReactionsBubble(
            context,
            canvas,
            width,
            isMyMessage = isMyMessage,
            isSingleReaction = true,
            inverseBubbleStyle = true
        )
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        this.reactionsViewStyle = ViewReactionsViewStyle(context, attrs)
        this.bubbleDrawer = ViewReactionsBubbleDrawer(reactionsViewStyle)

        setWillNotDraw(false)
        minimumHeight = reactionsViewStyle.totalHeight
    }
}
