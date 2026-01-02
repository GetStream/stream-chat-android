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

package io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl

import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import io.getstream.chat.android.ui.databinding.StreamUiItemLinkAttachmentBinding
import io.getstream.chat.android.ui.feature.messages.list.MessageListItemStyle
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListListeners
import io.getstream.chat.android.ui.feature.messages.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.feature.messages.list.internal.LongClickFriendlyLinkMovementMethod
import io.getstream.chat.android.ui.helper.transformer.ChatMessageTextTransformer
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.uiutils.extension.hasLink

/**
 * ViewHolder used for displaying messages that contain link attachments
 * and no other types of attachments.
 *
 * @param parent The parent container.
 * @param decorators List of decorators applied to the ViewHolder.
 * @param messageTextTransformer Formats strings and sets them on the respective TextView.
 * @param listeners Listeners used by the ViewHolder.
 * @param binding Binding generated for the layout.
 */
public class LinkAttachmentsViewHolder internal constructor(
    parent: ViewGroup,
    decorators: List<Decorator>,
    private val messageTextTransformer: ChatMessageTextTransformer,
    private val listeners: MessageListListeners?,
    public val binding: StreamUiItemLinkAttachmentBinding = StreamUiItemLinkAttachmentBinding.inflate(
        parent.streamThemeInflater,
        parent,
        false,
    ),
    public val style: MessageListItemStyle,
) : DecoratedBaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    /**
     * Initializes the ViewHolder class.
     */
    init {
        applyLinkAttachmentViewStyle()
        initializeListeners()
        setLinkMovementMethod()
    }

    override fun messageContainerView(): View = binding.messageContainer

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff) {
        super.bindData(data, diff)

        updateHorizontalBias(data)

        val linkAttachment = data.message.attachments.firstOrNull { attachment -> attachment.hasLink() }

        linkAttachment?.let { attachment ->
            binding.linkAttachmentView.showLinkAttachment(attachment, style)
            messageTextTransformer.transformAndApply(binding.messageText, data)
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
                messageContainer.setOnClickListener {
                    container.messageClickListener.onMessageClick(data.message)
                }
                reactionsView.setReactionClickListener {
                    container.reactionViewClickListener.onReactionViewClick(data.message)
                }
                footnote.setOnThreadClickListener {
                    container.threadClickListener.onThreadClick(data.message)
                }
                footnote.setOnTranslatedLabelClickListener {
                    container.translatedLabelClickListener.onTranslatedLabelClick(data.message)
                }
                messageContainer.setOnLongClickListener {
                    container.messageLongClickListener.onMessageLongClick(data.message)
                    true
                }
                userAvatarView.setOnClickListener {
                    container.userClickListener.onUserClick(data.message.user)
                }
                linkAttachmentView.setLinkPreviewClickListener {
                    listeners.linkClickListener.onLinkClick(it)
                }
                linkAttachmentView.setOnLongClickListener {
                    container.messageLongClickListener.onMessageLongClick(data.message)
                    true
                }
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
                onMentionClicked = container.mentionClickListener::onMentionClick,
            )
        }
    }

    /**
     * Applies styling to [io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal.LinkAttachmentView].
     */
    private fun applyLinkAttachmentViewStyle() {
        with(binding.linkAttachmentView) {
            setLinkDescriptionMaxLines(style.linkDescriptionMaxLines)
            setDescriptionTextStyle(style.textStyleLinkDescription)
            setTitleTextStyle(style.textStyleLinkTitle)
            setLabelTextStyle(style.textStyleLinkLabel)
        }
    }
}
