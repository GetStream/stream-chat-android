---
title: index
sidebar_position: 1
---
/[stream-chat-android-client](../index.md)/[io.getstream.chat.android.client.channel](index.md)  
  
  
  
# Package io.getstream.chat.android.client.channel  
  
  
## Types  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.channel/ChannelClient///PointingToDeclaration/"></a>[ChannelClient](ChannelClient/index.md)| <a name="io.getstream.chat.android.client.channel/ChannelClient///PointingToDeclaration/"></a>class [ChannelClient](ChannelClient/index.md)|
  
  
## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client.channel//subscribeFor/io.getstream.chat.android.client.channel.ChannelClient#io.getstream.chat.android.client.ChatEventListener[TypeParam(bounds=[io.getstream.chat.android.client.events.ChatEvent])]/PointingToDeclaration/"></a>[subscribeFor](subscribeFor.md)| <a name="io.getstream.chat.android.client.channel//subscribeFor/io.getstream.chat.android.client.channel.ChannelClient#io.getstream.chat.android.client.ChatEventListener[TypeParam(bounds=[io.getstream.chat.android.client.events.ChatEvent])]/PointingToDeclaration/"></a>inline fun &lt;[T](subscribeFor.md) : [ChatEvent](../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt; [ChannelClient](ChannelClient/index.md).[subscribeFor](subscribeFor.md)(listener: [ChatEventListener](../io.getstream.chat.android.client/ChatEventListener/index.md)&lt;[T](subscribeFor.md)&gt;): [Disposable](../io.getstream.chat.android.client.utils.observable/Disposable/index.md)Subscribes to events of type [T](subscribeFor.md) in the channel.inline fun &lt;[T](subscribeFor.md) : [ChatEvent](../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt; [ChannelClient](ChannelClient/index.md).[subscribeFor](subscribeFor.md)(lifecycleOwner: [LifecycleOwner](https://developer.android.com/reference/kotlin/androidx/lifecycle/LifecycleOwner.html), listener: [ChatEventListener](../io.getstream.chat.android.client/ChatEventListener/index.md)&lt;[T](subscribeFor.md)&gt;): [Disposable](../io.getstream.chat.android.client.utils.observable/Disposable/index.md)Subscribes to events of type [T](subscribeFor.md) in the channel, in the lifecycle of [lifecycleOwner](subscribeFor.md).fun [ChannelClient](ChannelClient/index.md).[subscribeFor](subscribeFor.md)(vararg eventTypes: [KClass](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)&lt;out [ChatEvent](../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt;, listener: [ChatEventListener](../io.getstream.chat.android.client/ChatEventListener/index.md)&lt;[ChatEvent](../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt;): [Disposable](../io.getstream.chat.android.client.utils.observable/Disposable/index.md)Subscribes to the specific [eventTypes](subscribeFor.md) of the channel.fun [ChannelClient](ChannelClient/index.md).[subscribeFor](subscribeFor.md)(lifecycleOwner: [LifecycleOwner](https://developer.android.com/reference/kotlin/androidx/lifecycle/LifecycleOwner.html), vararg eventTypes: [KClass](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)&lt;out [ChatEvent](../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt;, listener: [ChatEventListener](../io.getstream.chat.android.client/ChatEventListener/index.md)&lt;[ChatEvent](../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt;): [Disposable](../io.getstream.chat.android.client.utils.observable/Disposable/index.md)Subscribes to the specific [eventTypes](subscribeFor.md) of the channel, in the lifecycle of [lifecycleOwner](subscribeFor.md).|
| <a name="io.getstream.chat.android.client.channel//subscribeForSingle/io.getstream.chat.android.client.channel.ChannelClient#io.getstream.chat.android.client.ChatEventListener[TypeParam(bounds=[io.getstream.chat.android.client.events.ChatEvent])]/PointingToDeclaration/"></a>[subscribeForSingle](subscribeForSingle.md)| <a name="io.getstream.chat.android.client.channel//subscribeForSingle/io.getstream.chat.android.client.channel.ChannelClient#io.getstream.chat.android.client.ChatEventListener[TypeParam(bounds=[io.getstream.chat.android.client.events.ChatEvent])]/PointingToDeclaration/"></a>inline fun &lt;[T](subscribeForSingle.md) : [ChatEvent](../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt; [ChannelClient](ChannelClient/index.md).[subscribeForSingle](subscribeForSingle.md)(listener: [ChatEventListener](../io.getstream.chat.android.client/ChatEventListener/index.md)&lt;[T](subscribeForSingle.md)&gt;): [Disposable](../io.getstream.chat.android.client.utils.observable/Disposable/index.md)Subscribes for the next channel event of type [T](subscribeForSingle.md).|

