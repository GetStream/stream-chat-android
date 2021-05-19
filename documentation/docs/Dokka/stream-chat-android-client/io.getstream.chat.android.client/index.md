---
title: index
sidebar_position: 1
---
/[stream-chat-android-client](../index.md)/[io.getstream.chat.android.client](index.md)  
  
  
  
# Package io.getstream.chat.android.client  
  
  
## Types  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client/ChatClient///PointingToDeclaration/"></a>[ChatClient](ChatClient/index.md)| <a name="io.getstream.chat.android.client/ChatClient///PointingToDeclaration/"></a>class [ChatClient](ChatClient/index.md)The ChatClient is the main entry point for all low-level operations on chat|
| <a name="io.getstream.chat.android.client/ChatEventListener///PointingToDeclaration/"></a>[ChatEventListener](ChatEventListener/index.md)| <a name="io.getstream.chat.android.client/ChatEventListener///PointingToDeclaration/"></a>fun fun interface [ChatEventListener](ChatEventListener/index.md)&lt;[EventT](ChatEventListener/index.md) : [ChatEvent](../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt;|
  
  
## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.client//subscribeFor/io.getstream.chat.android.client.ChatClient#io.getstream.chat.android.client.ChatEventListener[TypeParam(bounds=[io.getstream.chat.android.client.events.ChatEvent])]/PointingToDeclaration/"></a>[subscribeFor](subscribeFor.md)| <a name="io.getstream.chat.android.client//subscribeFor/io.getstream.chat.android.client.ChatClient#io.getstream.chat.android.client.ChatEventListener[TypeParam(bounds=[io.getstream.chat.android.client.events.ChatEvent])]/PointingToDeclaration/"></a>inline fun &lt;[T](subscribeFor.md) : [ChatEvent](../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt; [ChatClient](ChatClient/index.md).[subscribeFor](subscribeFor.md)(listener: [ChatEventListener](ChatEventListener/index.md)&lt;[T](subscribeFor.md)&gt;): [Disposable](../io.getstream.chat.android.client.utils.observable/Disposable/index.md)Subscribes to client events of type [T](subscribeFor.md).inline fun &lt;[T](subscribeFor.md) : [ChatEvent](../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt; [ChatClient](ChatClient/index.md).[subscribeFor](subscribeFor.md)(lifecycleOwner: [LifecycleOwner](https://developer.android.com/reference/kotlin/androidx/lifecycle/LifecycleOwner.html), listener: [ChatEventListener](ChatEventListener/index.md)&lt;[T](subscribeFor.md)&gt;): [Disposable](../io.getstream.chat.android.client.utils.observable/Disposable/index.md)Subscribes to client events of type [T](subscribeFor.md), in the lifecycle of [lifecycleOwner](subscribeFor.md).fun [ChatClient](ChatClient/index.md).[subscribeFor](subscribeFor.md)(vararg eventTypes: [KClass](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)&lt;out [ChatEvent](../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt;, listener: [ChatEventListener](ChatEventListener/index.md)&lt;[ChatEvent](../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt;): [Disposable](../io.getstream.chat.android.client.utils.observable/Disposable/index.md)Subscribes to the specific [eventTypes](subscribeFor.md) of the client.fun [ChatClient](ChatClient/index.md).[subscribeFor](subscribeFor.md)(lifecycleOwner: [LifecycleOwner](https://developer.android.com/reference/kotlin/androidx/lifecycle/LifecycleOwner.html), vararg eventTypes: [KClass](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)&lt;out [ChatEvent](../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt;, listener: [ChatEventListener](ChatEventListener/index.md)&lt;[ChatEvent](../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt;): [Disposable](../io.getstream.chat.android.client.utils.observable/Disposable/index.md)Subscribes to the specific [eventTypes](subscribeFor.md) of the client, in the lifecycle of [lifecycleOwner](subscribeFor.md).|
| <a name="io.getstream.chat.android.client//subscribeForSingle/io.getstream.chat.android.client.ChatClient#io.getstream.chat.android.client.ChatEventListener[TypeParam(bounds=[io.getstream.chat.android.client.events.ChatEvent])]/PointingToDeclaration/"></a>[subscribeForSingle](subscribeForSingle.md)| <a name="io.getstream.chat.android.client//subscribeForSingle/io.getstream.chat.android.client.ChatClient#io.getstream.chat.android.client.ChatEventListener[TypeParam(bounds=[io.getstream.chat.android.client.events.ChatEvent])]/PointingToDeclaration/"></a>inline fun &lt;[T](subscribeForSingle.md) : [ChatEvent](../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt; [ChatClient](ChatClient/index.md).[subscribeForSingle](subscribeForSingle.md)(listener: [ChatEventListener](ChatEventListener/index.md)&lt;[T](subscribeForSingle.md)&gt;): [Disposable](../io.getstream.chat.android.client.utils.observable/Disposable/index.md)Subscribes for the next client event of type [T](subscribeForSingle.md).|

