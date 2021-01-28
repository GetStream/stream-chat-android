package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.util.Log
import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.uploader.ProgressTrackerFactory
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageFileAttachmentsBinding
import io.getstream.chat.android.ui.messages.adapter.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.messages.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.messages.adapter.view.AttachmentClickListener
import io.getstream.chat.android.ui.messages.adapter.view.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.messages.adapter.view.AttachmentLongClickListener
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

internal class OnlyFileAttachmentsViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    listeners: MessageListListenerContainer,
    internal val binding: StreamUiItemMessageFileAttachmentsBinding =
        StreamUiItemMessageFileAttachmentsBinding.inflate(
            parent.inflater,
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
            footnote.setOnThreadClickListener {
                listeners.threadClickListener.onThreadClick(data.message)
            }
            reactionsView.setReactionClickListener {
                listeners.reactionViewClickListener.onReactionViewClick(data.message)
            }
            fileAttachmentsView.attachmentClickListener = AttachmentClickListener {
                listeners.attachmentClickListener.onAttachmentClick(data.message, it)
            }
            fileAttachmentsView.attachmentDownloadClickListener = AttachmentDownloadClickListener {
                listeners.attachmentDownloadClickListener.onAttachmentDownloadClick(it)
            }

            root.setOnLongClickListener {
                listeners.messageLongClickListener.onMessageLongClick(data.message)
                true
            }
            fileAttachmentsView.attachmentLongClickListener = AttachmentLongClickListener {
                listeners.messageLongClickListener.onMessageLongClick(data.message)
            }
        }
    }

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, diff)

        binding.fileAttachmentsView.setAttachments(data.message.attachments)

        val uploadIdList = data.message
            .attachments
            .filter { attachment ->
                attachment.uploadState == Attachment.UploadState.InProgress
            }
            .mapNotNull { attachment ->
            attachment.uploadId
        }

        if (uploadIdList.isNotEmpty()) {
            clearScope()
            val scope = CoroutineScope(DispatcherProvider.Main)
            this.scope = scope

            scope.launch {
                var filesSent = 0
                val totalFiles = uploadIdList.size

                uploadIdList.forEach { uploadId ->
                    val tracker = ProgressTrackerFactory.getOrCreate(uploadId)

                    tracker.isComplete().filter { isComplete -> isComplete }.collect {
                        filesSent += 1

                        if (filesSent == totalFiles) {
                            Log.d("OnlyFileAttachments",
                                "Upload complete: ${context.getString(R.string.stream_ui_upload_complete)}")
                        } else {
                            Log.d("OnlyFileAttachments", "Upload sent: $filesSent / $totalFiles")
                        }
                    }
                }
            }
        }
    }

    private fun clearScope() {
        scope?.cancel()
        scope = null
    }

    override fun unbind() {
        super.unbind()
        clearScope()
    }
}
