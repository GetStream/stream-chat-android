package io.getstream.chat.android.ui.messages.adapter

import io.getstream.chat.android.ui.messages.view.MessageListView.AttachmentClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.GiphySendListener
import io.getstream.chat.android.ui.messages.view.MessageListView.MessageClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.MessageLongClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.MessageRetryListener
import io.getstream.chat.android.ui.messages.view.MessageListView.ReactionViewClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.ReadStateClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.UserClickListener

public interface ListenerContainer {
    public var messageClickListener: MessageClickListener
    public var messageLongClickListener: MessageLongClickListener
    public var messageRetryListener: MessageRetryListener
    public var attachmentClickListener: AttachmentClickListener
    public var reactionViewClickListener: ReactionViewClickListener
    public var userClickListener: UserClickListener
    public var readStateClickListener: ReadStateClickListener
    public var giphySendListener: GiphySendListener
}
