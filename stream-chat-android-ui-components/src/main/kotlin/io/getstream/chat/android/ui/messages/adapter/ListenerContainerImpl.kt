package io.getstream.chat.android.ui.messages.adapter

import com.getstream.sdk.chat.adapter.ListenerContainer
import com.getstream.sdk.chat.view.MessageListView.AttachmentClickListener
import com.getstream.sdk.chat.view.MessageListView.GiphySendListener
import com.getstream.sdk.chat.view.MessageListView.MessageClickListener
import com.getstream.sdk.chat.view.MessageListView.MessageLongClickListener
import com.getstream.sdk.chat.view.MessageListView.MessageLongClickListenerView
import com.getstream.sdk.chat.view.MessageListView.MessageRetryListener
import com.getstream.sdk.chat.view.MessageListView.ReactionViewClickListener
import com.getstream.sdk.chat.view.MessageListView.ReadStateClickListener
import com.getstream.sdk.chat.view.MessageListView.UserClickListener
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

public class ListenerContainerImpl(
    messageClickListener: MessageClickListener = MessageClickListener(EmptyFunctions.ONE_PARAM),
    messageLongClickListener: MessageLongClickListener = MessageLongClickListener(EmptyFunctions.ONE_PARAM),
    messageLongClickListenerView: MessageLongClickListenerView = MessageLongClickListenerView(EmptyFunctions.TWO_PARAM),
    messageRetryListener: MessageRetryListener = MessageRetryListener(EmptyFunctions.ONE_PARAM),
    attachmentClickListener: AttachmentClickListener = AttachmentClickListener(EmptyFunctions.TWO_PARAM),
    reactionViewClickListener: ReactionViewClickListener = ReactionViewClickListener(EmptyFunctions.ONE_PARAM),
    userClickListener: UserClickListener = UserClickListener(EmptyFunctions.ONE_PARAM),
    readStateClickListener: ReadStateClickListener = ReadStateClickListener(EmptyFunctions.ONE_PARAM),
    giphySendListener: GiphySendListener = GiphySendListener(EmptyFunctions.TWO_PARAM)
) : ListenerContainer {
    private object EmptyFunctions {
        val ONE_PARAM: (Any) -> Unit = { _ -> Unit }
        val TWO_PARAM: (Any, Any?) -> Unit = { _, _ -> Unit }
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

    override var messageLongClickListenerView: MessageLongClickListenerView by ListenerDelegate(
        messageLongClickListenerView
    ) { realListener ->
        MessageLongClickListenerView { message, view ->
            realListener().onMessageLongClick2(message, view)
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

    /**
     * A property delegate to be used with listeners.
     *
     * The real listener stored in [realListener] isn't exposed externally, it's only
     * accessible through the [wrapper].
     *
     * The [wrapper] is exposed by the getter, and a reference to it can be safely stored
     * long-term.
     *
     * Setting new listeners via the setter will update the underlying listener, and
     * calls to the [wrapper] will then be forwarded to the latest [realListener] that
     * was set.
     *
     * @param wrap A function that has to produce the wrapper listener. The listener being
     *             wrapped can be referenced by calling the realListener() method. This
     *             function always returns the current listener, even if it changes.
     */
    internal class ListenerDelegate<L : Any>(
        initialValue: L,
        wrap: (realListener: () -> L) -> L
    ) : ReadWriteProperty<Any?, L> {

        private var realListener: L = initialValue
        private val wrapper: L = wrap { realListener }

        override fun getValue(thisRef: Any?, property: KProperty<*>): L {
            return wrapper
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: L) {
            realListener = value
        }
    }
}
