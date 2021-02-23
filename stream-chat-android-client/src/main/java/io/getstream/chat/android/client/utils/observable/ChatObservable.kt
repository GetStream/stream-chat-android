package io.getstream.chat.android.client.utils.observable

import io.getstream.chat.android.client.events.ChatEvent

@Deprecated(
    message = "Use the subscribe methods on ChatClient or ChannelController directly instead of events()",
    level = DeprecationLevel.ERROR,
)
@Suppress("DEPRECATION_ERROR")
public interface ChatObservable {

    @Deprecated(
        message = "Use the subscribe methods on ChatClient or ChannelController directly instead of events()",
        level = DeprecationLevel.ERROR,
    )
    public fun filter(eventType: String): ChatObservable

    @Deprecated(
        message = "Use the subscribe methods on ChatClient or ChannelController directly instead of events()",
        level = DeprecationLevel.ERROR,
    )
    public fun filter(predicate: (event: ChatEvent) -> Boolean): ChatObservable

    @Deprecated(
        message = "Use the subscribe methods on ChatClient or ChannelController directly instead of events()",
        level = DeprecationLevel.ERROR,
    )
    public fun filter(vararg types: Class<out ChatEvent>): ChatObservable

    @Deprecated(
        message = "Use the subscribe methods on ChatClient or ChannelController directly instead of events()",
        level = DeprecationLevel.ERROR,
    )
    public fun first(): ChatObservable

    @Deprecated(
        message = "Use the subscribe methods on ChatClient or ChannelController directly instead of events()",
        level = DeprecationLevel.ERROR,
    )
    public fun subscribe(listener: (ChatEvent) -> Unit): Subscription

    @Deprecated(
        message = "Use the subscribe methods on ChatClient or ChannelController directly instead of events()",
        level = DeprecationLevel.ERROR,
    )
    public fun unsubscribe(subscription: Subscription)

    @Deprecated(
        message = "Use the subscribe methods on ChatClient or ChannelController directly instead of events()",
        level = DeprecationLevel.ERROR,
    )
    public fun ignoreInitState(): ChatObservable
}
