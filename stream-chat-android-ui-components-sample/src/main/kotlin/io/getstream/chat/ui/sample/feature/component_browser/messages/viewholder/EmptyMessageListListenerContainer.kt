package io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder

import io.getstream.chat.android.ui.messages.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.messages.view.MessageListView.AttachmentClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.GiphySendListener
import io.getstream.chat.android.ui.messages.view.MessageListView.MessageClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.MessageLongClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.MessageRetryListener
import io.getstream.chat.android.ui.messages.view.MessageListView.ReactionViewClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.ThreadClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.UserClickListener

/**
 * Dummy implementation for Component Browser use only
 */
internal object EmptyMessageListListenerContainer : MessageListListenerContainer {
    override val messageClickListener: MessageClickListener =
        MessageClickListener {}
    override val messageLongClickListener: MessageLongClickListener =
        MessageLongClickListener {}
    override val messageRetryListener: MessageRetryListener =
        MessageRetryListener {}
    override val threadClickListener: ThreadClickListener =
        ThreadClickListener {}
    override val attachmentClickListener: AttachmentClickListener =
        AttachmentClickListener { _, _ -> }
    override val attachmentDownloadClickListener: AttachmentDownloadClickListener =
        AttachmentDownloadClickListener {}
    override val reactionViewClickListener: ReactionViewClickListener =
        ReactionViewClickListener {}
    override val userClickListener: UserClickListener =
        UserClickListener {}
    override val giphySendListener: GiphySendListener =
        GiphySendListener { _, _ -> }
}
