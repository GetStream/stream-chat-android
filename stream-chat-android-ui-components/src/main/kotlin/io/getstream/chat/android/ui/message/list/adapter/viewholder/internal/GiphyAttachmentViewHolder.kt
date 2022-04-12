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

package io.getstream.chat.android.ui.message.list.adapter.viewholder.internal

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.isBottomPosition
import io.getstream.chat.android.ui.common.extensions.hasText
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.extensions.isReply
import io.getstream.chat.android.ui.databinding.StreamUiItemGiphyAttachmentBinding
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.view.internal.GiphyMediaAttachmentView
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.BackgroundDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator
import io.getstream.chat.android.ui.transformer.ChatMessageTextTransformer

/**
 * Represents the Giphy attachment holder, when the Giphy is already sent.
 *
 * @param parent The parent container.
 * @param decorators List of decorators for various parts of the holder.
 * @param listeners The listeners for various user interactions.
 * @param markdown Markdown renderer, used for the message text.
 * @param binding The binding that holds all the View references.
 */
internal class GiphyAttachmentViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    private val listeners: MessageListListenerContainer?,
    private val markdown: ChatMessageTextTransformer,
    internal val binding: StreamUiItemGiphyAttachmentBinding = StreamUiItemGiphyAttachmentBinding.inflate(
        parent.streamThemeInflater,
        parent,
        false
    ),
) : DecoratedBaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    init {
        initializeListeners()
    }

    /**
     * Initializes listeners that enable handling clicks on various
     * elements such as reactions, threads, message containers, etc.
     */
    private fun initializeListeners() {
        binding.run {
            listeners?.let { container ->
                root.setOnClickListener {
                    data.message.attachments.firstOrNull()?.let { attachment ->
                        container.attachmentClickListener.onAttachmentClick(data.message, attachment)
                    }
                }
                reactionsView.setReactionClickListener {
                    container.reactionViewClickListener.onReactionViewClick(data.message)
                }
                footnote.setOnThreadClickListener {
                    container.threadClickListener.onThreadClick(data.message)
                }
                root.setOnLongClickListener {
                    container.messageLongClickListener.onMessageLongClick(data.message)
                    true
                }
                avatarView.setOnClickListener {
                    container.userClickListener.onUserClick(data.message.user)
                }
            }
        }
    }

    /**
     * Binds the data required to represent a Giphy attachment. Shows the Giphy, applies decorations and the transformations
     * for text and the container shape.
     *
     * Loads the Giphy once things are set up.
     *
     * @param data The data that holds all the information required to show a Giphy.
     * @param diff The difference from the previous draw.
     */
    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, diff)

        binding.messageText.isVisible = data.message.text.isNotEmpty()
        markdown.transformAndApply(binding.messageText, data)

        binding.messageContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
            horizontalBias = if (data.isTheirs) 0f else 1f
        }

        imageCorners(binding.mediaAttachmentView, data)

        val attachment = data.message.attachments.first()
        binding.mediaAttachmentView.showGiphy(attachment = attachment)
    }

    /**
     * Decorates the image corners of the [GiphyMediaAttachmentView].
     *
     * @param mediaAttachmentView The View to decorate the corners of.
     * @param data The data that holds all the information about the Giphy message.
     */
    private fun imageCorners(mediaAttachmentView: GiphyMediaAttachmentView, data: MessageListItem.MessageItem) {
        val topLeftCorner = if (data.message.isReply()) 0f else BackgroundDecorator.DEFAULT_CORNER_RADIUS
        val topRightCorner = if (data.message.isReply()) 0f else BackgroundDecorator.DEFAULT_CORNER_RADIUS
        val bottomRightCorner = if (data.message.hasText() || (data.isMine && data.isBottomPosition())) {
            0f
        } else {
            BackgroundDecorator.DEFAULT_CORNER_RADIUS
        }
        val bottomLeftCorner = if (data.message.hasText() || (data.isTheirs && data.isBottomPosition())) {
            0f
        } else {
            BackgroundDecorator.DEFAULT_CORNER_RADIUS
        }

        mediaAttachmentView.setImageShapeByCorners(topLeftCorner, topRightCorner, bottomRightCorner, bottomLeftCorner)
    }
}
