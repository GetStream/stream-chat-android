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

import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.LongClickFriendlyLinkMovementMethod
import io.getstream.chat.android.ui.databinding.StreamUiItemFileAttachmentsBinding
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentLongClickListener
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator
import io.getstream.chat.android.ui.transformer.ChatMessageTextTransformer

/**
 * ViewHolder that displays message items containing file attachments.
 *
 * Note: This ViewHolder is used in situations where the message either contains
 * multiple attachment types or a single attachment type that does not have
 * a designated ViewHolder.
 *
 * You can see the full list of ViewHolders in [io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewHolderFactory].
 *
 * @param parent The parent container.
 * @param decorators List of decorators applied to the ViewHolder.
 * @param messageTextTransformer Formats strings and sets them on the respective TextView.
 * @param listeners Listeners used by the ViewHolder.
 * @param binding Binding generated for the layout.
 */
internal class FileAttachmentsViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    private val listeners: MessageListListenerContainer?,
    private val messageTextTransformer: ChatMessageTextTransformer,
    internal val binding: StreamUiItemFileAttachmentsBinding = StreamUiItemFileAttachmentsBinding.inflate(
        parent.streamThemeInflater,
        parent,
        false
    ),
) : DecoratedBaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    /**
     * Initializes the ViewHolder class.
     */
    init {
        initializeListeners()
        setLinkMovementMethod()
        binding.fileAttachmentsView.setPadding(4.dpToPx())
    }

    /**
     * Binds the data to the view.
     */
    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, diff)

        updateHorizontalBias(data)

        binding.fileAttachmentsView.setAttachments(data.message.attachments)

        if (data.message.text.isNotEmpty()) {
            messageTextTransformer.transformAndApply(binding.messageText, data)
            binding.messageText.visibility = View.VISIBLE
        } else {
            binding.messageText.visibility = View.GONE
        }
    }

    /**
     * Updates the horizontal bias of the message according to the owner
     * of the message.
     */
    private fun updateHorizontalBias(data: MessageListItem.MessageItem) {
        binding.messageContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
            this.horizontalBias = if (data.isMine) 1f else 0f
        }
    }

    /**
     * Initializes listeners that enable handling clicks on various
     * elements such as reactions, threads, message containers, etc.
     */
    private fun initializeListeners() {
        binding.run {
            listeners?.let { container ->
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
                binding.fileAttachmentsView.attachmentLongClickListener = AttachmentLongClickListener {
                    container.messageLongClickListener.onMessageLongClick(data.message)
                }
                binding.fileAttachmentsView.attachmentClickListener = AttachmentClickListener { attachment ->
                    container.attachmentClickListener.onAttachmentClick(data.message, attachment)
                }
                binding.fileAttachmentsView.attachmentDownloadClickListener =
                    AttachmentDownloadClickListener(container.attachmentDownloadClickListener::onAttachmentDownloadClick)
            }
        }
    }

    /**
     * Enables clicking on links.
     */
    private fun setLinkMovementMethod() {
        listeners?.let { listenerContainer ->
            LongClickFriendlyLinkMovementMethod.set(
                textView = binding.messageText,
                longClickTarget = binding.root,
                onLinkClicked = listenerContainer.linkClickListener::onLinkClick
            )
        }
    }
}
