package io.getstream.chat.android.ui.messages.adapter

import android.view.View
import com.getstream.sdk.chat.adapter.MessageListItem

public open class MessageListItemViewHolderFactory {
    public fun createViewHolder(parentView: View, viewType: Int): BaseMessageItemViewHolder<*> {
        return when (MessageListItemViewTypeMapper.viewTypeValueToViewType(viewType)) {
            MessageListItemViewType.DATE_DIVIDER -> createDateDividerViewHolder(parentView)
            MessageListItemViewType.MESSAGE_DELETED -> createMessageDeletedViewHolder(parentView)
            MessageListItemViewType.PLAIN_TEXT -> createPlainTextViewHolder(parentView)
            MessageListItemViewType.REPLY_MESSAGE -> createReplyMessageViewHolder(parentView)
            MessageListItemViewType.PLAIN_TEXT_WITH_ATTACHMENTS -> createPlainTextWithAttachmentsViewHolder(parentView)
            MessageListItemViewType.ATTACHMENTS -> createAttachmentsViewHolder(parentView)
            MessageListItemViewType.LOADING_INDICATOR -> createLoadingIndicatorViewHolder(parentView)
            MessageListItemViewType.THREAD_SEPARATOR -> createThreadSeparatorViewHolder(parentView)
        }
    }

    public open fun createDateDividerViewHolder(parentView: View): BaseMessageItemViewHolder<*> = createBaseMessageItemViewHolder(parentView)
    public open fun createMessageDeletedViewHolder(parentView: View): BaseMessageItemViewHolder<*> = createBaseMessageItemViewHolder(parentView)
    public open fun createPlainTextViewHolder(parentView: View): BaseMessageItemViewHolder<*> = createBaseMessageItemViewHolder(parentView)
    public open fun createReplyMessageViewHolder(parentView: View): BaseMessageItemViewHolder<*> = createBaseMessageItemViewHolder(parentView)
    public open fun createPlainTextWithAttachmentsViewHolder(parentView: View): BaseMessageItemViewHolder<*> = createBaseMessageItemViewHolder(parentView)
    public open fun createAttachmentsViewHolder(parentView: View): BaseMessageItemViewHolder<*> = createBaseMessageItemViewHolder(parentView)
    public open fun createLoadingIndicatorViewHolder(parentView: View): BaseMessageItemViewHolder<*> = createBaseMessageItemViewHolder(parentView)
    public open fun createThreadSeparatorViewHolder(parentView: View): BaseMessageItemViewHolder<*> = createBaseMessageItemViewHolder(parentView)

    private fun createBaseMessageItemViewHolder(parentView: View): BaseMessageItemViewHolder<MessageListItem> {
        return object : BaseMessageItemViewHolder<MessageListItem>(parentView) {
            override fun bindData(data: MessageListItem) = Unit
        }
    }
}
