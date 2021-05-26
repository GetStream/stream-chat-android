package com.getstream.sdk.chat.adapter

import com.getstream.sdk.chat.view.MessageListView.AttachmentClickListener
import com.getstream.sdk.chat.view.MessageListView.GiphySendListener
import com.getstream.sdk.chat.view.MessageListView.MessageClickListener
import com.getstream.sdk.chat.view.MessageListView.MessageLongClickListener
import com.getstream.sdk.chat.view.MessageListView.MessageRetryListener
import com.getstream.sdk.chat.view.MessageListView.ReactionViewClickListener
import com.getstream.sdk.chat.view.MessageListView.ReadStateClickListener
import com.getstream.sdk.chat.view.MessageListView.UserClickListener

public sealed interface ListenerContainer {
    public var messageClickListener: MessageClickListener
    public var messageLongClickListener: MessageLongClickListener
    public var messageRetryListener: MessageRetryListener
    public var attachmentClickListener: AttachmentClickListener
    public var reactionViewClickListener: ReactionViewClickListener
    public var userClickListener: UserClickListener
    public var readStateClickListener: ReadStateClickListener
    public var giphySendListener: GiphySendListener
}
