package com.getstream.sdk.chat.adapter

import com.getstream.sdk.chat.utils.ListenerDelegate
import com.getstream.sdk.chat.view.MessageListView.AttachmentClickListener
import com.getstream.sdk.chat.view.MessageListView.GiphySendListener
import com.getstream.sdk.chat.view.MessageListView.MessageClickListener
import com.getstream.sdk.chat.view.MessageListView.MessageLongClickListener
import com.getstream.sdk.chat.view.MessageListView.MessageRetryListener
import com.getstream.sdk.chat.view.MessageListView.ReactionViewClickListener
import com.getstream.sdk.chat.view.MessageListView.ReadStateClickListener
import com.getstream.sdk.chat.view.MessageListView.UserClickListener

internal class ListenerContainerImpl(
    messageClickListener: MessageClickListener = MessageClickListener(EmptyFunctions.ONE_PARAM),
    messageLongClickListener: MessageLongClickListener = MessageLongClickListener(EmptyFunctions.ONE_PARAM),
    messageRetryListener: MessageRetryListener = MessageRetryListener(EmptyFunctions.ONE_PARAM),
    attachmentClickListener: AttachmentClickListener = AttachmentClickListener(EmptyFunctions.TWO_PARAM),
    reactionViewClickListener: ReactionViewClickListener = ReactionViewClickListener(EmptyFunctions.ONE_PARAM),
    userClickListener: UserClickListener = UserClickListener(EmptyFunctions.ONE_PARAM),
    readStateClickListener: ReadStateClickListener = ReadStateClickListener(EmptyFunctions.ONE_PARAM),
    giphySendListener: GiphySendListener = GiphySendListener(EmptyFunctions.TWO_PARAM)
) : ListenerContainer {
    private object EmptyFunctions {
        val ONE_PARAM: (Any) -> Unit = { _ -> Unit }
        val TWO_PARAM: (Any, Any) -> Unit = { _, _ -> Unit }
    }

    override var messageClickListener: MessageClickListener by ListenerDelegate(
        messageClickListener
    ) { realListener ->
        MessageClickListener { message ->
            realListener().onMessageClick(message)
        }
    }

    override var messageLongClickListener: MessageLongClickListener by ListenerDelegate(
        messageLongClickListener
    ) { realListener ->
        MessageLongClickListener { message ->
            realListener().onMessageLongClick(message)
        }
    }

    override var messageRetryListener: MessageRetryListener by ListenerDelegate(
        messageRetryListener
    ) { realListener ->
        MessageRetryListener { message ->
            realListener().onRetryMessage(message)
        }
    }

    override var attachmentClickListener: AttachmentClickListener by ListenerDelegate(
        attachmentClickListener
    ) { realListener ->
        AttachmentClickListener { message, attachment ->
            realListener().onAttachmentClick(message, attachment)
        }
    }

    override var reactionViewClickListener: ReactionViewClickListener by ListenerDelegate(
        reactionViewClickListener
    ) { realListener ->
        ReactionViewClickListener { message ->
            realListener().onReactionViewClick(message)
        }
    }

    override var userClickListener: UserClickListener by ListenerDelegate(
        userClickListener
    ) { realListener ->
        UserClickListener { user ->
            realListener().onUserClick(user)
        }
    }

    override var readStateClickListener: ReadStateClickListener by ListenerDelegate(
        readStateClickListener
    ) { realListener ->
        ReadStateClickListener { reads ->
            realListener().onReadStateClick(reads)
        }
    }

    override var giphySendListener: GiphySendListener by ListenerDelegate(
        giphySendListener
    ) { realListener ->
        GiphySendListener { message, action ->
            realListener().onGiphySend(message, action)
        }
    }
}
