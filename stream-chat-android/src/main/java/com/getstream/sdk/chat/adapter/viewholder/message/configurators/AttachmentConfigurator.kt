package com.getstream.sdk.chat.adapter.viewholder.message.configurators

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.adapter.AttachmentViewHolderFactory
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.adapter.viewholder.message.hasNoAttachments
import com.getstream.sdk.chat.adapter.viewholder.message.isDeleted
import com.getstream.sdk.chat.adapter.viewholder.message.isFailed
import com.getstream.sdk.chat.databinding.StreamItemMessageBinding
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.logger.ChatLogger

internal class AttachmentConfigurator(
    private val binding: StreamItemMessageBinding,
    private val style: MessageListViewStyle,
    private val viewHolderFactory: AttachmentViewHolderFactory
) : Configurator {

    override fun configure(messageItem: MessageItem) {
        configAttachmentView(messageItem)
    }

    private fun configAttachmentView(messageItem: MessageItem) {
        val message = messageItem.message

        val deletedMessage = message.isDeleted()
        val failedMessage = message.isFailed()
        val noAttachments = message.hasNoAttachments()
        if (deletedMessage || failedMessage || noAttachments) {
            ChatLogger.instance.logE(
                tag = javaClass.simpleName,
                message = "attachment hidden: deletedMessage:$deletedMessage, failedMessage:$failedMessage noAttachments:$noAttachments"
            )
            binding.attachmentview.isVisible = false
            return
        }

        binding.attachmentview.apply {
            updateLayoutParams<ConstraintLayout.LayoutParams> {
                horizontalBias = if (messageItem.isTheirs) 0f else 1f
            }
            isVisible = true
            init(viewHolderFactory, style)
            setEntity(messageItem)
        }
    }
}
