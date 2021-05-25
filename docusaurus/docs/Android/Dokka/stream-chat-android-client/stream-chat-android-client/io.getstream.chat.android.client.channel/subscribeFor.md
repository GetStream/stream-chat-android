---
title: subscribeFor
---
//[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client.channel](index.md)/[subscribeFor](subscribeFor.md)



# subscribeFor  
[androidJvm]  
Content  
inline fun &lt;[T](subscribeFor.md) : [ChatEvent](../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt; [ChannelClient](ChannelClient/index.md).[subscribeFor](subscribeFor.md)(listener: [ChatEventListener](../io.getstream.chat.android.client/ChatEventListener/index.md)&lt;[T](subscribeFor.md)&gt;): [Disposable](../io.getstream.chat.android.client.utils.observable/Disposable/index.md)  
More info  


Subscribes to events of type [T](subscribeFor.md) in the channel.

  


[androidJvm]  
Content  
inline fun &lt;[T](subscribeFor.md) : [ChatEvent](../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt; [ChannelClient](ChannelClient/index.md).[subscribeFor](subscribeFor.md)(lifecycleOwner: [LifecycleOwner](https://developer.android.com/reference/kotlin/androidx/lifecycle/LifecycleOwner.html), listener: [ChatEventListener](../io.getstream.chat.android.client/ChatEventListener/index.md)&lt;[T](subscribeFor.md)&gt;): [Disposable](../io.getstream.chat.android.client.utils.observable/Disposable/index.md)  
More info  


Subscribes to events of type [T](subscribeFor.md) in the channel, in the lifecycle of [lifecycleOwner](subscribeFor.md).



Only receives events when the lifecycle is in a STARTED state, otherwise events are dropped.

  


[androidJvm]  
Content  
fun [ChannelClient](ChannelClient/index.md).[subscribeFor](subscribeFor.md)(vararg eventTypes: [KClass](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)&lt;out [ChatEvent](../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt;, listener: [ChatEventListener](../io.getstream.chat.android.client/ChatEventListener/index.md)&lt;[ChatEvent](../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt;): [Disposable](../io.getstream.chat.android.client.utils.observable/Disposable/index.md)  
More info  


Subscribes to the specific [eventTypes](subscribeFor.md) of the channel.

  


[androidJvm]  
Content  
fun [ChannelClient](ChannelClient/index.md).[subscribeFor](subscribeFor.md)(lifecycleOwner: [LifecycleOwner](https://developer.android.com/reference/kotlin/androidx/lifecycle/LifecycleOwner.html), vararg eventTypes: [KClass](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)&lt;out [ChatEvent](../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt;, listener: [ChatEventListener](../io.getstream.chat.android.client/ChatEventListener/index.md)&lt;[ChatEvent](../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt;): [Disposable](../io.getstream.chat.android.client.utils.observable/Disposable/index.md)  
More info  


Subscribes to the specific [eventTypes](subscribeFor.md) of the channel, in the lifecycle of [lifecycleOwner](subscribeFor.md).



Only receives events when the lifecycle is in a STARTED state, otherwise events are dropped.

  



