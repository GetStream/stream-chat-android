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

package io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment

import android.content.Context
import android.view.View
import io.getstream.chat.android.models.Message

/**
 * An inner ViewHolder for custom attachments within message ViewHolder.
 * Follows the lifecycle of the outer message ViewHolder.
 *
 * @param itemView The view that this ViewHolder controls.
 */
public abstract class InnerAttachmentViewHolder(public val itemView: View) {
    public val context: Context = itemView.context

    /**
     * Called when RecyclerView binds the parent message ViewHolder.
     *
     * @param message The message whose attachments will be displayed.
     */
    public open fun onBindViewHolder(message: Message) {}

    /**
     * Called when RecyclerView recycles the parent message ViewHolder.
     */
    public open fun onUnbindViewHolder() {}

    /**
     * Called when a view in this ViewHolder has been attached to a window.
     */
    public open fun onViewAttachedToWindow() {}

    /**
     * Called when a view in this ViewHolder has been detached from its window.
     */
    public open fun onViewDetachedFromWindow() {}

    public companion object {
        /**
         * Creates a stub [InnerAttachmentViewHolder] instance.
         *
         * @param itemView The view that this ViewHolder controls.
         */
        public fun stub(itemView: View): InnerAttachmentViewHolder {
            return object : InnerAttachmentViewHolder(itemView) {}
        }
    }
}
