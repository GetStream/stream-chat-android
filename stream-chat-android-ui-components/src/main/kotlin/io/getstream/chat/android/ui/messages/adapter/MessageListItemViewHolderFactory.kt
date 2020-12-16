package io.getstream.chat.android.ui.messages.adapter

import android.view.View
import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.ListenerContainer
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.messages.adapter.viewholder.DateDividerViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessageDeletedViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder

public open class MessageListItemViewHolderFactory {

    public var listenerContainer: ListenerContainer? = null

    public fun createViewHolder(parentView: ViewGroup, viewType: Int): BaseMessageItemViewHolder<*> {
        return when (MessageListItemViewTypeMapper.viewTypeValueToViewType(viewType)) {
            MessageListItemViewType.DATE_DIVIDER -> createDateDividerViewHolder(parentView)
            MessageListItemViewType.MESSAGE_DELETED -> createMessageDeletedViewHolder(parentView)
            MessageListItemViewType.PLAIN_TEXT -> createPlainTextViewHolder(parentView)
            MessageListItemViewType.REPLY_MESSAGE -> createReplyMessageViewHolder(parentView)
            MessageListItemViewType.PLAIN_TEXT_WITH_FILE_ATTACHMENTS -> createPlainTextWithFileAttachmentsViewHolder(parentView)
            MessageListItemViewType.PLAIN_TEXT_WITH_MEDIA_ATTACHMENTS -> createPlainTextWithMediaAttachmentsViewHolder(parentView)
            MessageListItemViewType.MEDIA_ATTACHMENTS -> createMediaAttachmentsViewHolder(parentView)
            MessageListItemViewType.ATTACHMENTS -> createAttachmentsViewHolder(parentView)
            MessageListItemViewType.LOADING_INDICATOR -> createLoadingIndicatorViewHolder(parentView)
            MessageListItemViewType.THREAD_SEPARATOR -> createThreadSeparatorViewHolder(parentView)
        }
    }

    public open fun createDateDividerViewHolder(parentView: ViewGroup): BaseMessageItemViewHolder<*> {
        return DateDividerViewHolder(parentView)
    }

    public open fun createMessageDeletedViewHolder(parentView: ViewGroup): BaseMessageItemViewHolder<*> {
        return MessageDeletedViewHolder(parentView)
    }

    public open fun createPlainTextViewHolder(parentView: ViewGroup): BaseMessageItemViewHolder<*> {
        return MessagePlainTextViewHolder(parentView, listenerContainer)
    }

    public open fun createReplyMessageViewHolder(parentView: ViewGroup): BaseMessageItemViewHolder<*> {
        return createBaseMessageItemViewHolder(parentView)
    }

    public open fun createPlainTextWithFileAttachmentsViewHolder(parentView: ViewGroup): BaseMessageItemViewHolder<*> {
        return createBaseMessageItemViewHolder(parentView)
    }

    private fun createPlainTextWithMediaAttachmentsViewHolder(parentView: ViewGroup): BaseMessageItemViewHolder<*> {
        return PlainTextWithMediaAttachmentsViewHolder(parentView, listenerContainer)
    }

    public open fun createMediaAttachmentsViewHolder(parentView: ViewGroup): BaseMessageItemViewHolder<*> {
        return OnlyMediaAttachmentsViewHolder(parentView, listenerContainer)
    }

    public open fun createAttachmentsViewHolder(parentView: ViewGroup): BaseMessageItemViewHolder<*> {
        return createBaseMessageItemViewHolder(parentView)
    }

    public open fun createLoadingIndicatorViewHolder(parentView: ViewGroup): BaseMessageItemViewHolder<*> {
        return createBaseMessageItemViewHolder(parentView)
    }

    public open fun createThreadSeparatorViewHolder(parentView: ViewGroup): BaseMessageItemViewHolder<*> {
        return createBaseMessageItemViewHolder(parentView)
    }

    private fun createBaseMessageItemViewHolder(parentView: ViewGroup): BaseMessageItemViewHolder<MessageListItem> {
        return object : BaseMessageItemViewHolder<MessageListItem>(View(parentView.context)) {
            override fun bindData(data: MessageListItem, diff: MessageListItemPayloadDiff?) = Unit
        }
    }
}
