package io.getstream.chat.android.ui.message.list.adapter.viewholder.internal

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.LongClickFriendlyLinkMovementMethod
import io.getstream.chat.android.ui.common.markdown.ChatMarkdown
import io.getstream.chat.android.ui.databinding.StreamUiItemTextAndAttachmentsBinding
import io.getstream.chat.android.ui.message.list.MessageListItemStyle
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentViewFactory
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

internal class TextAndAttachmentsViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    private val listeners: MessageListListenerContainer,
    private val markdown: ChatMarkdown,
    private val attachmentViewFactory: AttachmentViewFactory,
    private val style: MessageListItemStyle,
    internal val binding: StreamUiItemTextAndAttachmentsBinding = StreamUiItemTextAndAttachmentsBinding.inflate(
        parent.streamThemeInflater,
        parent,
        false
    ),
) : DecoratedBaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    private var scope: CoroutineScope? = null

    init {
        binding.run {
            root.setOnClickListener {
                listeners.messageClickListener.onMessageClick(data.message)
            }
            reactionsView.setReactionClickListener {
                listeners.reactionViewClickListener.onReactionViewClick(data.message)
            }
            footnote.setOnThreadClickListener {
                listeners.threadClickListener.onThreadClick(data.message)
            }
            root.setOnLongClickListener {
                listeners.messageLongClickListener.onMessageLongClick(data.message)
                true
            }
            avatarView.setOnClickListener {
                listeners.userClickListener.onUserClick(data.message.user)
            }
            LongClickFriendlyLinkMovementMethod.set(
                textView = messageText,
                longClickTarget = root,
                onLinkClicked = listeners.linkClickListener::onLinkClick
            )
        }
    }

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, diff)

        binding.messageText.isVisible = data.message.text.isNotEmpty()
        markdown.setText(binding.messageText, data.message.text)

        setupAttachment(data)
        setupUploads(data)
    }

    private fun setupAttachment(data: MessageListItem.MessageItem) {
        with(binding.attachmentsContainer) {
            removeAllViews()
            addView(attachmentViewFactory.createAttachmentView(data, listeners, style, binding.root))
        }
    }

    private fun clearScope() {
        scope?.cancel()
        scope = null
    }

    override fun unbind() {
        clearScope()
        super.unbind()
    }

    private fun setupUploads(data: MessageListItem.MessageItem) {
        val totalAttachmentsCount = data.message.attachments.size
        var uploadedAttachmentsCount = 0
        data.message.attachments.forEach {
            if (it.uploadState == Attachment.UploadState.Success) {
                uploadedAttachmentsCount++
            }
        }
        if (uploadedAttachmentsCount == totalAttachmentsCount) {
            binding.sentFiles.text =
                context.getString(R.string.stream_ui_message_list_attachment_upload_complete)
        } else {
            binding.sentFiles.text =
                context.getString(
                    R.string.stream_ui_message_list_attachment_uploading,
                    uploadedAttachmentsCount,
                    totalAttachmentsCount
                )
        }
    }

    override fun onDetachedFromWindow() {
        clearScope()
    }

    override fun onAttachedToWindow() {
        setupUploads(data)
    }

    private companion object {
        private suspend fun trackFilesSent(
            context: Context,
            uploadIdList: List<String>,
            sentFilesView: TextView,
        ) {
            /*val filesSent = 0
            val totalFiles = uploadIdList.size

            sentFilesView.isVisible = true
            sentFilesView.text =
                context.getString(R.string.stream_ui_message_list_attachment_uploading, filesSent, totalFiles)

            val completionFlows: List<Flow<Boolean>> = uploadIdList.map { uploadId ->
                ProgressTrackerFactory.getOrCreate(uploadId).isComplete()
            }

            combine(completionFlows) { isCompleteArray ->
                isCompleteArray.count { isComplete -> isComplete }
            }.collect { completedCount ->
                if (completedCount == totalFiles) {
                    sentFilesView.text =
                        context.getString(R.string.stream_ui_message_list_attachment_upload_complete)
                } else {
                    sentFilesView.text =
                        context.getString(
                            R.string.stream_ui_message_list_attachment_uploading,
                            completedCount,
                            totalFiles
                        )
                }
            }*/
        }
    }
}
