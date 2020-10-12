package com.getstream.sdk.chat.adapter

import com.getstream.sdk.chat.view.MessageListView.AttachmentClickListener
import com.getstream.sdk.chat.view.MessageListView.GiphySendListener
import com.getstream.sdk.chat.view.MessageListView.MessageClickListener
import com.getstream.sdk.chat.view.MessageListView.MessageLongClickListener
import com.getstream.sdk.chat.view.MessageListView.MessageRetryListener
import com.getstream.sdk.chat.view.MessageListView.ReactionViewClickListener
import com.getstream.sdk.chat.view.MessageListView.ReadStateClickListener
import com.getstream.sdk.chat.view.MessageListView.UserClickListener

interface ListenerContainer {
    var messageClickListener: MessageClickListener
    var messageLongClickListener: MessageLongClickListener
    var messageRetryListener: MessageRetryListener
    var attachmentClickListener: AttachmentClickListener
    var reactionViewClickListener: ReactionViewClickListener
    var userClickListener: UserClickListener
    var readStateClickListener: ReadStateClickListener
    var giphySendListener: GiphySendListener
}
