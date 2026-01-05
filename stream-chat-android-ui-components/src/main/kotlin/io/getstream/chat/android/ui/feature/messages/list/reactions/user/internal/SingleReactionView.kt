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

package io.getstream.chat.android.ui.feature.messages.list.reactions.user.internal

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageReactionBinding
import io.getstream.chat.android.ui.feature.messages.list.reactions.user.SingleReactionViewStyle
import io.getstream.chat.android.ui.feature.messages.list.reactions.view.MessageOptionsUserReactionOrientation
import io.getstream.chat.android.ui.feature.messages.list.reactions.view.getUserReactionOrientation
import io.getstream.chat.android.ui.feature.messages.list.reactions.view.isOrientedTowardsStart
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

internal class SingleReactionView : FrameLayout {
    private val binding = StreamUiItemMessageReactionBinding.inflate(streamThemeInflater, this, true)
    private lateinit var reactionsViewStyle: SingleReactionViewStyle
    private lateinit var bubbleDrawer: SingleReactionViewBubbleDrawer
    private var isMyMessage: Boolean = false
    private val messageOrientation: MessageOptionsUserReactionOrientation
        get() = reactionsViewStyle.reactionOrientation.getUserReactionOrientation()

    constructor(context: Context) : super(context.createStreamThemeWrapper()) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
    ) {
        init(context, attrs)
    }

    fun setReaction(userReactionItem: UserReactionItem) {
        // according to the design, current user reactions have the same style
        // as reactions on the current user messages in the message list
        this.isMyMessage = userReactionItem.isMine
        binding.reactionIcon.setImageDrawable(userReactionItem.drawable)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val isOrientedTowardsStart = messageOrientation.isOrientedTowardsStart(isMyMessage)

        bubbleDrawer.drawReactionsBubble(
            context = context,
            canvas = canvas,
            bubbleWidth = width,
            isMyMessage = isMyMessage,
            isOrientedTowardsStart = isOrientedTowardsStart,
        )
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        this.reactionsViewStyle = SingleReactionViewStyle(context, attrs)
        this.bubbleDrawer = SingleReactionViewBubbleDrawer(reactionsViewStyle)

        setWillNotDraw(false)
        minimumHeight = reactionsViewStyle.totalHeight
    }
}
