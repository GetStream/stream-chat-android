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

package io.getstream.chat.android.ui.feature.messages.list.adapter

import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.ui.common.internal.animateHighlight

public abstract class BaseMessageItemViewHolder<T : MessageListItem>(
    itemView: View,
) : RecyclerView.ViewHolder(itemView) {

    /**
     * The data that was last bound to this ViewHolder via [bindData].
     * Can be used for listeners that need to pass along the currently
     * bound data as a parameter.
     */
    protected lateinit var data: T
        private set

    private var highlightAnimation: ValueAnimator? = null

    /**
     * Returns the Message container that we can use to anchor UI based on its size and position.
     */
    public open fun messageContainerView(): View? {
        return null
    }

    /**
     * Workaround to allow a downcast of the MessageListItem to T.
     */
    @Suppress("UNCHECKED_CAST")
    internal fun bindListItem(messageListItem: MessageListItem, diff: MessageListItemPayloadDiff? = null) {
        messageListItem as T

        this.data = messageListItem
        bindData(messageListItem, diff)
    }

    public abstract fun bindData(data: T, diff: MessageListItemPayloadDiff?)

    @CallSuper
    public open fun unbind() {
        cancelHighlightAnimation()
    }

    internal fun startHighlightAnimation() {
        highlightAnimation = itemView.animateHighlight()
    }

    private fun cancelHighlightAnimation() {
        highlightAnimation?.cancel()
        highlightAnimation = null
    }

    protected val context: Context = itemView.context

    /**
     * Called when this view holder and its' view were detached from window.
     */
    public open fun onDetachedFromWindow() {}

    /**
     * Called when this view holder and its' view were attached to window.
     */
    public open fun onAttachedToWindow() {}
}
