package com.getstream.sdk.chat.adapter

import com.getstream.sdk.chat.view.MessageListView.MessageClickListener
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class ListenerContainer(
    // Empty initial listeners by default
    messageClickListener: MessageClickListener = MessageClickListener(EMPTY_TWO_PARAM)
) {

    private companion object {
        val EMPTY_TWO_PARAM = { _: Any, _: Any -> Unit }
    }

    // The actual listener instance
    private var _messageClickListener: MessageClickListener = messageClickListener

    // Manually implemented setter for the actual listener,
    // as we never want it to be read externally
    fun setMessageClickListener(messageClickListener: MessageClickListener) {
        _messageClickListener = messageClickListener
    }

    // Wrapper that can be referenced externally, even stored, as it will
    // always point to the actual listener, even if that changes
    val messageClickListener: MessageClickListener =
        MessageClickListener { message, position ->
            _messageClickListener.onMessageClick(message, position)
        }
}

/* ************************** */
/* ************************** */
/* ************************** */

class ListenerContainerV2(
    messageClickListener: MessageClickListener = MessageClickListener(EMPTY_TWO_PARAM)
) {
    private companion object {
        val EMPTY_TWO_PARAM = { _: Any, _: Any -> Unit }
    }

    // Delegate to wrap up all of the above in a single property per listener
    var messageClickListener: MessageClickListener by ListenerDelegate(
        initialValue = messageClickListener,
        wrap = { realListener ->
            MessageClickListener { message, position ->
                realListener().onMessageClick(message, position)
            }
        }
    )
}

internal class ListenerDelegate<L : Any>(
    initialValue: L,
    // A function that has to produce the wrapper listener. The listener to wrap
    // can be referenced by calling the realListener() method. This always returns
    // the actual listener, even if it changes.
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
