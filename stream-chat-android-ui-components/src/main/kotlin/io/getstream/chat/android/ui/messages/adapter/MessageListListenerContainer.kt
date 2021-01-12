package io.getstream.chat.android.ui.messages.adapter

import io.getstream.chat.android.ui.messages.view.MessageListView
import io.getstream.chat.android.ui.messages.view.MessageListView.AttachmentClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.GiphySendListener
import io.getstream.chat.android.ui.messages.view.MessageListView.LinkClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.MessageClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.MessageLongClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.MessageRetryListener
import io.getstream.chat.android.ui.messages.view.MessageListView.ReactionViewClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.UserClickListener

public interface MessageListListenerContainer {
    public val messageClickListener: MessageClickListener
    public val messageLongClickListener: MessageLongClickListener
    public val messageRetryListener: MessageRetryListener
    public val threadClickListener: MessageListView.ThreadClickListener
    public val attachmentClickListener: AttachmentClickListener
    public val attachmentDownloadClickListener: AttachmentDownloadClickListener
    public val reactionViewClickListener: ReactionViewClickListener
    public val userClickListener: UserClickListener
    public val giphySendListener: GiphySendListener
    public val linkClickListener: LinkClickListener
}
