package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.view.setPadding
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.common.extensions.internal.hasLink
import io.getstream.chat.android.ui.common.extensions.internal.isMedia
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentLongClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.FileAttachmentsView
import io.getstream.chat.android.ui.message.list.adapter.view.internal.LinkAttachmentView
import io.getstream.chat.android.ui.message.list.adapter.view.internal.MediaAttachmentsGroupView
import io.getstream.chat.android.ui.message.list.internal.MessageListItemStyle

/**
 * Factory for creating content views for links attachments and other types of attachments.
 */
public open class AttachmentViewFactory {

    public open fun createAttachmentViews(
        data: MessageListItem.MessageItem,
        listeners: MessageListListenerContainer,
        style: MessageListItemStyle,
        parent: View,
    ): Pair<View?, View?> {
        require(data.message.attachments.isNotEmpty()) { "Can't create content view for empty attachments" }
        val (links, attachments) = data.message.attachments.partition(Attachment::hasLink)

        val firstView = if (attachments.isNotEmpty()) {
            val attachmentView = createAttachmentsView(attachments, parent.context)
            when (attachmentView) {
                is MediaAttachmentsGroupView -> setupMediaAttachmentView(attachmentView, attachments, listeners, data)
                is FileAttachmentsView -> setupFileAttachmentsView(attachmentView, attachments, listeners, data.message)
            }
            attachmentView
        } else {
            null
        }

        val linkView = links.firstOrNull()?.let { setupLinkView(it, data.isMine, listeners, style, parent) }

        return firstView to linkView
    }

    /**
     * Creates a content view for the link attachment type.
     *
     * @param linkAttachment Attachment representing some link.
     * @param context Context related to parent view.
     */
    protected fun createLinkAttachmentView(linkAttachment: Attachment, context: Context): View {
        require(linkAttachment.hasLink()) { "Can create link view only for attachments with link" }
        return LinkAttachmentView(context).apply {
            layoutParams = DEFAULT_LAYOUT_PARAMS
        }
    }

    /**
     * Creates a content view for general attachments.
     *
     * @param attachments List of attachments. Resulting view should represents this list.
     * @param context Context related to parent view.
     */
    protected fun createAttachmentsView(attachments: List<Attachment>, context: Context): View {
        return when {
            attachments.isMedia() -> MediaAttachmentsGroupView(context).apply {
                layoutParams = DEFAULT_LAYOUT_PARAMS
            }
            attachments.isNotEmpty() -> FileAttachmentsView(context).apply {
                layoutParams = DEFAULT_LAYOUT_PARAMS
            }
            else -> error("Unsupported case for attachment view factory!")
        }
    }

    private fun setupMediaAttachmentView(
        mediaAttachmentsGroupView: MediaAttachmentsGroupView,
        attachments: List<Attachment>,
        listeners: MessageListListenerContainer,
        data: MessageListItem.MessageItem,
    ) = mediaAttachmentsGroupView.run {
        setPadding(MEDIA_ATTACHMENT_VIEW_PADDING)
        setupBackground(data)
        attachmentClickListener = AttachmentClickListener {
            listeners.attachmentClickListener.onAttachmentClick(data.message, it)
        }
        attachmentLongClickListener = AttachmentLongClickListener {
            listeners.messageLongClickListener.onMessageLongClick(data.message)
        }
        showAttachments(attachments)
    }

    private fun setupFileAttachmentsView(
        fileAttachmentsView: FileAttachmentsView,
        attachments: List<Attachment>,
        listeners: MessageListListenerContainer,
        message: Message,
    ) = fileAttachmentsView.run {
        setPadding(FILE_ATTACHMENT_VIEW_PADDING)
        attachmentLongClickListener = AttachmentLongClickListener {
            listeners.messageLongClickListener.onMessageLongClick(message)
        }
        attachmentClickListener = AttachmentClickListener {
            listeners.attachmentClickListener.onAttachmentClick(message, it)
        }
        attachmentDownloadClickListener = AttachmentDownloadClickListener {
            listeners.attachmentDownloadClickListener.onAttachmentDownloadClick(it)
        }
        setAttachments(attachments)
    }

    private fun setupLinkView(
        linkAttachment: Attachment,
        isMine: Boolean,
        listeners: MessageListListenerContainer,
        style: MessageListItemStyle,
        parent: View,
    ): View {
        val linkAttachmentView = createLinkAttachmentView(linkAttachment, parent.context)
        (linkAttachmentView as? LinkAttachmentView)?.run {
            setPadding(LINK_VIEW_PADDING)
            setLinkPreviewClickListener { url ->
                listeners.linkClickListener.onLinkClick(url)
            }
            setLongClickTarget(parent)
            style.getStyleTextColor(isMine)?.also(::setTextColor)
            showLinkAttachment(linkAttachment)
        }
        return linkAttachmentView
    }

    private companion object {

        private fun Collection<Attachment>.isMedia(): Boolean =
            isNotEmpty() && all { it.isMedia() && it.hasLink().not() }

        private val MEDIA_ATTACHMENT_VIEW_PADDING = 1.dpToPx()
        private val LINK_VIEW_PADDING = 8.dpToPx()
        private val FILE_ATTACHMENT_VIEW_PADDING = 4.dpToPx()

        private val DEFAULT_LAYOUT_PARAMS =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
