package io.getstream.chat.android.ui.messages.adapter

import com.getstream.sdk.chat.utils.ListenerDelegate
import io.getstream.chat.android.ui.messages.view.MessageListView.AttachmentClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.GiphySendListener
import io.getstream.chat.android.ui.messages.view.MessageListView.MessageClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.MessageLongClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.MessageRetryListener
import io.getstream.chat.android.ui.messages.view.MessageListView.ReactionViewClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.ReadStateClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.UserClickListener

internal class MessageListListenerContainerImpl(
    messageClickListener: MessageClickListener = MessageClickListener(EmptyFunctions.ONE_PARAM),
    messageLongClickListener: MessageLongClickListener = MessageLongClickListener(EmptyFunctions.ONE_PARAM),
    messageRetryListener: MessageRetryListener = MessageRetryListener(EmptyFunctions.ONE_PARAM),
    attachmentClickListener: AttachmentClickListener = AttachmentClickListener(EmptyFunctions.TWO_PARAM),
    reactionViewClickListener: ReactionViewClickListener = ReactionViewClickListener(EmptyFunctions.ONE_PARAM),
    userClickListener: UserClickListener = UserClickListener(EmptyFunctions.ONE_PARAM),
    readStateClickListener: ReadStateClickListener = ReadStateClickListener(EmptyFunctions.ONE_PARAM),
    giphySendListener: GiphySendListener = GiphySendListener(EmptyFunctions.TWO_PARAM)
) : MessageListListenerContainer {
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
