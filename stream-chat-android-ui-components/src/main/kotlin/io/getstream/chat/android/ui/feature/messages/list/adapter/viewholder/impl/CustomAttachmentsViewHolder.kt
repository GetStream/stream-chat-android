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

package io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl

import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import io.getstream.chat.android.ui.databinding.StreamUiItemCustomAttachmentsBinding
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListListeners
import io.getstream.chat.android.ui.feature.messages.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.AttachmentFactoryManager
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.InnerAttachmentViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.feature.messages.list.internal.LongClickFriendlyLinkMovementMethod
import io.getstream.chat.android.ui.helper.transformer.ChatMessageTextTransformer
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

/**
 * ViewHolder used for displaying messages that contain custom attachments.
 *
 * @param parent The parent container.
 * @param decorators List of decorators applied to the ViewHolder.
 * @param listeners Listeners used by the ViewHolder.
 * @param messageTextTransformer Formats strings and sets them on the respective TextView.
 * @param attachmentFactoryManager A manager for the registered custom attachment factories.
 * @param binding Binding generated for the layout.
 */
public class CustomAttachmentsViewHolder internal constructor(
    parent: ViewGroup,
    decorators: List<Decorator>,
    private val listeners: MessageListListeners?,
    private val messageTextTransformer: ChatMessageTextTransformer,
    private val attachmentFactoryManager: AttachmentFactoryManager,
    public val binding: StreamUiItemCustomAttachmentsBinding = StreamUiItemCustomAttachmentsBinding.inflate(
        parent.streamThemeInflater,
        parent,
        false,
    ),
) : DecoratedBaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    /**
     * The inner ViewHolder with custom attachments.
     */
    private var innerAttachmentViewHolder: InnerAttachmentViewHolder? = null

    /**
     * Initializes the ViewHolder class.
     */
    init {
        initializeListeners()
        setLinkMovementMethod()
    }

    override fun messageContainerView(): View = binding.messageContainer

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff) {
        super.bindData(data, diff)
        bindMessageText()
        bindHorizontalBias()
        bindCustomAttachments(data)
    }

    /**
     * Updates the text section of the message.
     */
    private fun bindMessageText() {
        binding.messageText.isVisible = data.message.text.isNotEmpty()
        messageTextTransformer.transformAndApply(binding.messageText, data)
    }

    /**
     * Updates the horizontal bias of the message according to the owner
     * of the message.
     */
    private fun bindHorizontalBias() {
        binding.messageContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
            this.horizontalBias = if (data.isMine) 1f else 0f
        }
    }

    /**
     * Updates the custom attachments section of the message.
     */
    private fun bindCustomAttachments(data: MessageListItem.MessageItem) {
        // TODO this seems to be inefficient, we should probably cache the view holder
        //  instead of creating it on every `bindCustomAttachments` call.
        this.innerAttachmentViewHolder =
            attachmentFactoryManager.createViewHolder(data.message, listeners, binding.root)
                .also { attachmentViewHolder ->
                    attachmentViewHolder.onBindViewHolder(data.message)
                    binding.attachmentsContainer.removeAllViews()
                    binding.attachmentsContainer.addView(attachmentViewHolder.itemView)
                }
    }

    /**
     * Initializes listeners that enable handling clicks on various
     * elements such as reactions, threads, message containers, etc.
     */
    private fun initializeListeners() {
        binding.run {
            listeners?.let { container ->
                messageContainer.setOnClickListener {
                    container.messageClickListener.onMessageClick(data.message)
                }
                reactionsView.setReactionClickListener {
                    container.reactionViewClickListener.onReactionViewClick(data.message)
                }
                footnote.setOnThreadClickListener {
                    container.threadClickListener.onThreadClick(data.message)
                }
                messageContainer.setOnLongClickListener {
                    container.messageLongClickListener.onMessageLongClick(data.message)
                    true
                }
                userAvatarView.setOnClickListener {
                    container.userClickListener.onUserClick(data.message.user)
                }
                LongClickFriendlyLinkMovementMethod.set(
                    textView = messageText,
                    longClickTarget = messageContainer,
                    onLinkClicked = container.linkClickListener::onLinkClick,
                )
            }
        }
    }

    /**
     * Enables clicking on links.
     */
    private fun setLinkMovementMethod() {
        listeners?.let { container ->
            LongClickFriendlyLinkMovementMethod.set(
                textView = binding.messageText,
                longClickTarget = binding.messageContainer,
                onLinkClicked = container.linkClickListener::onLinkClick,
            )
        }
    }

    /**
     * Called when a view in this ViewHolder has been recycled.
     */
    override fun unbind() {
        innerAttachmentViewHolder?.onUnbindViewHolder()
        super.unbind()
    }

    /**
     * Called when a view in this ViewHolder has been attached to a window.
     */
    override fun onDetachedFromWindow() {
        innerAttachmentViewHolder?.onViewDetachedFromWindow()
    }

    /**
     * Called when a view in this ViewHolder has been detached from its window.
     */
    override fun onAttachedToWindow() {
        innerAttachmentViewHolder?.onViewAttachedToWindow()
    }
}
