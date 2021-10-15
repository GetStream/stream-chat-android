package io.getstream.chat.android.ui.message.list.adapter.viewholder.internal

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.uploader.ProgressTrackerFactory
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.isMedia
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.LongClickFriendlyLinkMovementMethod
import io.getstream.chat.android.ui.common.markdown.ChatMarkdown
import io.getstream.chat.android.ui.databinding.StreamUiItemTextAndAttachmentsBinding
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.attachments.FileAttachmentsAdapter
import io.getstream.chat.android.ui.message.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentViewHolderFactory
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

internal class TextAndAttachmentsViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    private val listeners: MessageListListenerContainer,
    private val markdown: ChatMarkdown,
    private val attachmentViewHolderFactory: AttachmentViewHolderFactory,
    private val recycledViewPool: RecyclerView.RecycledViewPool,
    internal val binding: StreamUiItemTextAndAttachmentsBinding = StreamUiItemTextAndAttachmentsBinding.inflate(
        parent.streamThemeInflater,
        parent,
        false
    ),
) : DecoratedBaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    private var scope: CoroutineScope? = null

    private lateinit var adapter: FileAttachmentsAdapter

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
        attachmentViewHolderFactory.setUp(data.message.attachments)
        adapter = FileAttachmentsAdapter(attachmentViewHolderFactory)

        binding.run {
            attachmentsRecycler.layoutManager = LinearLayoutManager(context).apply {
                recycleChildrenOnDetach = true
            }
            attachmentsRecycler.setRecycledViewPool(recycledViewPool)
            attachmentsRecycler.adapter = adapter
        }

        //Fix this with a different base adapter for AttachmentGroup
        val attachmentGroups = if (data.message.attachments.isMedia()) {
            listOf(data.message.attachments)
        } else {
            data.message.attachments.map { attachment ->
                listOf(attachment)
            }
        }

        adapter.setItems(attachmentGroups)
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
        val uploadIdList: List<String> = data.message.attachments
            .filter { attachment -> attachment.uploadState == Attachment.UploadState.InProgress }
            .mapNotNull(Attachment::uploadId)

        val needUpload = uploadIdList.isNotEmpty()

        if (needUpload) {
            clearScope()
            val scope = CoroutineScope(DispatcherProvider.Main)
            this.scope = scope

            scope.launch {
                trackFilesSent(context, uploadIdList, binding.sentFiles)
            }
        } else {
            binding.sentFiles.isVisible = false
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
            val filesSent = 0
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
            }
        }
    }
}
