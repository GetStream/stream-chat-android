package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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

    /**
     * Create a content view for particular collection of attachments. If the collection contains only link attachment
     * then it creates a link content attachment view, if the collection contains attachments without links then it
     * creates a content view for the list of attachments, otherwise it creates a content view with both links and list
     * contents views.
     *
     * @param data [MessageListItem.MessageItem] with particular data for the message list.
     * @param listeners [MessageListListenerContainer] with listeners for the message list.
     * @param style [MessageListItemStyle] style container with text colors params for the message list.
     * @param parent [View] of VH's root where such attachment content view is supposed to be placed.
     */
    public open fun createAttachmentViews(
        data: MessageListItem.MessageItem,
        listeners: MessageListListenerContainer,
        style: MessageListItemStyle,
        parent: View,
    ): View {
        val (links, attachments) = data.message.attachments.partition(Attachment::hasLink)

        return when {
            links.isNotEmpty() && attachments.isNotEmpty() -> createLinkAndAttachmentsContent(
                attachments,
                links.first(),
                data,
                listeners,
                style,
                parent
            )
            links.isNotEmpty() -> createLinkView(links.first(), data.isMine, listeners, style, parent)
            attachments.isNotEmpty() -> createAndSetupAttachmentsView(data, listeners, attachments, parent)
            else -> error("Can't create content view for the empty attachments collection")
        }
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

    private fun createLinkAndAttachmentsContent(
        attachments: List<Attachment>,
        linkAttachment: Attachment,
        data: MessageListItem.MessageItem,
        listeners: MessageListListenerContainer,
        style: MessageListItemStyle,
        parent: View,
    ): View {
        return LinearLayout(parent.context).apply {
            layoutParams = DEFAULT_LAYOUT_PARAMS
            addView(createAndSetupAttachmentsView(data, listeners, attachments, parent))
            addView(createLinkView(linkAttachment, data.isMine, listeners, style, parent))
        }
    }

    private fun createAndSetupAttachmentsView(
        data: MessageListItem.MessageItem,
        listeners: MessageListListenerContainer,
        attachments: List<Attachment>,
        parent: View,
    ): View {
        return createAttachmentsView(attachments, parent.context).also {
            when (it) {
                is MediaAttachmentsGroupView -> setupMediaAttachmentView(it, attachments, listeners, data)
                is FileAttachmentsView -> setupFileAttachmentsView(it, attachments, listeners, data.message)
            }
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

    private fun createLinkView(
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
        private fun Collection<Attachment>.isMedia(): Boolean = isNotEmpty() && all(Attachment::isMedia)

        private val MEDIA_ATTACHMENT_VIEW_PADDING = 1.dpToPx()
        private val LINK_VIEW_PADDING = 8.dpToPx()
        private val FILE_ATTACHMENT_VIEW_PADDING = 4.dpToPx()

        private val DEFAULT_LAYOUT_PARAMS =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
