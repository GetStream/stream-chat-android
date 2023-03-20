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

package io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal

import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageMediaAttachmentBinding
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.feature.messages.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal.AttachmentClickListener
import io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal.AttachmentLongClickListener
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.internal.Decorator
import io.getstream.chat.android.ui.feature.messages.list.internal.LongClickFriendlyLinkMovementMethod
import io.getstream.chat.android.ui.helper.transformer.ChatMessageTextTransformer
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

/**
 * ViewHolder used for displaying messages that contain image and/or video attachments.
 *
 * @param parent The parent container.
 * @param decorators List of decorators applied to the ViewHolder.
 * @param listeners Listeners used by the ViewHolder.
 * @param messageTextTransformer Formats strings and sets them on the respective TextView.
 * @param binding Binding generated for the layout.
 */
internal class MediaAttachmentsViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    private val listeners: MessageListListenerContainer?,
    private val messageTextTransformer: ChatMessageTextTransformer,
    internal val binding: StreamUiItemMessageMediaAttachmentBinding = StreamUiItemMessageMediaAttachmentBinding.inflate(
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
    }

    override fun messageContainerView(): View = binding.messageContainer

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, diff)

        bindMessageText()
        bindHorizontalBias()
        if (diff?.attachments != false) {
            bindMediaAttachments()
            bindAudioRecordAttachments()
        }
        bindUploadingIndicator()
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
     * Updates the media attachments section of the message.
     */
    private fun bindMediaAttachments() {
        binding.mediaAttachmentView.setPadding(1.dpToPx())
        binding.mediaAttachmentView.setupBackground(data)
        binding.mediaAttachmentView.showAttachments(data.message.attachments)
    }

    private fun bindAudioRecordAttachments() {
        binding.audioRecordsView.showAudioAttachments(data.message.attachments)
    }

    /**
     * Update the uploading status section of the message.
     */
    private fun bindUploadingIndicator() {
        val totalAttachmentsCount = data.message.attachments.size
        val completedAttachmentsCount =
            data.message.attachments.count {
                it.uploadState == null || it.uploadState == Attachment.UploadState.Success
            }
        if (completedAttachmentsCount == totalAttachmentsCount) {
            binding.sentFiles.isVisible = false
        } else {
            binding.sentFiles.text =
                context.getString(
                    R.string.stream_ui_message_list_attachment_uploading,
                    completedAttachmentsCount,
                    totalAttachmentsCount
                )
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
                mediaAttachmentView.attachmentClickListener = AttachmentClickListener { attachment ->
                    container.attachmentClickListener.onAttachmentClick(data.message, attachment)
                }
                mediaAttachmentView.attachmentLongClickListener = AttachmentLongClickListener {
                    container.messageLongClickListener.onMessageLongClick(data.message)
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
                onLinkClicked = container.linkClickListener::onLinkClick
            )
        }
    }

    override fun onAttachedToWindow() {
        bindUploadingIndicator()
    }
}
