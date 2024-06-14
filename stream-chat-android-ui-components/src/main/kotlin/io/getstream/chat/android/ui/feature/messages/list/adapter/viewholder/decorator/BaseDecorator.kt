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

package io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator

import io.getstream.chat.android.core.internal.exhaustive
import io.getstream.chat.android.ui.feature.messages.list.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.CustomAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.DateDividerViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.FileAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.GiphyAttachmentViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.GiphyViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.LinkAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.MediaAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.MessageDeletedViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.PollViewHolder

public abstract class BaseDecorator : Decorator {

    public override fun <T : MessageListItem> decorate(
        viewHolder: BaseMessageItemViewHolder<T>,
        data: T,
    ) {
        if (data !is MessageListItem.MessageItem) {
            return
        }
        when (viewHolder) {
            is MessageDeletedViewHolder -> decorateDeletedMessage(viewHolder, data)
            is MessagePlainTextViewHolder -> decoratePlainTextMessage(viewHolder, data)
            is CustomAttachmentsViewHolder -> decorateCustomAttachmentsMessage(viewHolder, data)
            is LinkAttachmentsViewHolder -> decorateLinkAttachmentsMessage(viewHolder, data)
            is GiphyViewHolder -> decorateGiphyMessage(viewHolder, data)
            is GiphyAttachmentViewHolder -> decorateGiphyAttachmentMessage(viewHolder, data)
            is FileAttachmentsViewHolder -> decorateFileAttachmentsMessage(viewHolder, data)
            is MediaAttachmentsViewHolder -> decorateMediaAttachmentsMessage(viewHolder, data)
            is PollViewHolder -> decoratePollMessage(viewHolder, data)
            is DateDividerViewHolder -> Unit
            else -> Unit
        }.exhaustive
    }

    /**
     * Applies various decorations to the [CustomAttachmentsViewHolder].
     *
     * @param viewHolder The holder to be decorated.
     * @param data The data used to define various decorations.
     */
    protected abstract fun decorateCustomAttachmentsMessage(
        viewHolder: CustomAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    )

    /**
     * Applies various decorations to the [GiphyAttachmentViewHolder].
     *
     * @param viewHolder The holder to be decorated.
     * @param data The data used to define various decorations.
     */
    protected abstract fun decorateGiphyAttachmentMessage(
        viewHolder: GiphyAttachmentViewHolder,
        data: MessageListItem.MessageItem,
    )

    /**
     * Applies various decorations to the [FileAttachmentsViewHolder].
     *
     * @param viewHolder The holder to be decorated.
     * @param data The data used to define various decorations.
     */
    protected abstract fun decorateFileAttachmentsMessage(
        viewHolder: FileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    )

    /**
     * Applies various decorations to the [MediaAttachmentsViewHolder].
     *
     * @param viewHolder The holder to be decorated.
     * @param data The data used to define various decorations.
     */
    protected abstract fun decorateMediaAttachmentsMessage(
        viewHolder: MediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    )

    protected abstract fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
    )

    /**
     * Applies various decorations to [LinkAttachmentsViewHolder].
     *
     * @param viewHolder The holder to be decorated.
     * @param data The data used to define various decorations.
     */
    protected abstract fun decorateLinkAttachmentsMessage(
        viewHolder: LinkAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    )

    /**
     * Applies various decorations to the [MessageDeletedViewHolder].
     *
     * @param viewHolder The holder to be decorated.
     * @param data The data used to define various decorations.
     */
    protected open fun decorateDeletedMessage(
        viewHolder: MessageDeletedViewHolder,
        data: MessageListItem.MessageItem,
    ): Unit = Unit

    /**
     * Applies various decorations to the [GiphyViewHolder].
     *
     * @param viewHolder The holder to be decorated.
     * @param data The data used to define various decorations.
     */
    protected open fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
        data: MessageListItem.MessageItem,
    ): Unit = Unit

    /**
     * Applies various decorations to the [PollViewHolder].
     *
     * @param viewHolder The holder to be decorated.
     * @param data The data used to define various decorations.
     */
    protected open fun decoratePollMessage(
        viewHolder: PollViewHolder,
        data: MessageListItem.MessageItem,
    ): Unit = Unit
}
