package io.getstream.chat.android.client.utils.observable

import io.getstream.chat.android.client.events.ChatEvent

@Deprecated("Use the subscribe methods on ChatClient or ChannelController directly instead of events()")
public interface ChatObservable {

    @Deprecated("Use the subscribe methods on ChatClient or ChannelController directly instead of events()")
    public fun filter(eventType: String): ChatObservable
    @Deprecated("Use the subscribe methods on ChatClient or ChannelController directly instead of events()")
    public fun filter(predicate: (event: ChatEvent) -> Boolean): ChatObservable
    @Deprecated("Use the subscribe methods on ChatClient or ChannelController directly instead of events()")
    public fun filter(vararg types: Class<out ChatEvent>): ChatObservable
    @Deprecated("Use the subscribe methods on ChatClient or ChannelController directly instead of events()")
    public fun first(): ChatObservable
    @Deprecated("Use the subscribe methods on ChatClient or ChannelController directly instead of events()")
    public fun subscribe(listener: (ChatEvent) -> Unit): Subscription
    @Deprecated("Use the subscribe methods on ChatClient or ChannelController directly instead of events()")
    public fun unsubscribe(subscription: Subscription)
    @Deprecated("Use the subscribe methods on ChatClient or ChannelController directly instead of events()")
    public fun ignoreInitState(): ChatObservable
}
