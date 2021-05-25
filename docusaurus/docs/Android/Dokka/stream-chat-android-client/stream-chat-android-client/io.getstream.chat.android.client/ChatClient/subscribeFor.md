---
title: subscribeFor
---
//[stream-chat-android-client](../../../index.md)/[io.getstream.chat.android.client](../index.md)/[ChatClient](index.md)/[subscribeFor](subscribeFor.md)



# subscribeFor  
[androidJvm]  
Content  
fun [subscribeFor](subscribeFor.md)(vararg eventTypes: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), listener: (event: [ChatEvent](../../io.getstream.chat.android.client.events/ChatEvent/index.md)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)): [Disposable](../../io.getstream.chat.android.client.utils.observable/Disposable/index.md)  
More info  


Subscribes to the specific [eventTypes](subscribeFor.md) of the client.



#### Since Kotlin  
99999.9  
  


## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client/ChatClient/subscribeFor/#kotlin.Array[kotlin.String]#kotlin.Function1[io.getstream.chat.android.client.events.ChatEvent,kotlin.Unit]/PointingToDeclaration/"></a>[io.getstream.chat.android.client.models.EventType](../../io.getstream.chat.android.client.models/EventType/index.md)| <a name="io.getstream.chat.android.client/ChatClient/subscribeFor/#kotlin.Array[kotlin.String]#kotlin.Function1[io.getstream.chat.android.client.events.ChatEvent,kotlin.Unit]/PointingToDeclaration/"></a><br/><br/>for type constants<br/><br/>|
  
  


[androidJvm]  
Content  
fun [subscribeFor](subscribeFor.md)(vararg eventTypes: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), listener: [ChatEventListener](../ChatEventListener/index.md)&lt;[ChatEvent](../../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt;): [Disposable](../../io.getstream.chat.android.client.utils.observable/Disposable/index.md)  
More info  


Subscribes to the specific [eventTypes](subscribeFor.md) of the client.



## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.client/ChatClient/subscribeFor/#kotlin.Array[kotlin.String]#io.getstream.chat.android.client.ChatEventListener[io.getstream.chat.android.client.events.ChatEvent]/PointingToDeclaration/"></a>[io.getstream.chat.android.client.models.EventType](../../io.getstream.chat.android.client.models/EventType/index.md)| <a name="io.getstream.chat.android.client/ChatClient/subscribeFor/#kotlin.Array[kotlin.String]#io.getstream.chat.android.client.ChatEventListener[io.getstream.chat.android.client.events.ChatEvent]/PointingToDeclaration/"></a><br/><br/>for type constants<br/><br/>|
  
  


[androidJvm]  
Content  
fun [subscribeFor](subscribeFor.md)(lifecycleOwner: [LifecycleOwner](https://developer.android.com/reference/kotlin/androidx/lifecycle/LifecycleOwner.html), vararg eventTypes: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), listener: (event: [ChatEvent](../../io.getstream.chat.android.client.events/ChatEvent/index.md)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)): [Disposable](../../io.getstream.chat.android.client.utils.observable/Disposable/index.md)  
fun [subscribeFor](subscribeFor.md)(lifecycleOwner: [LifecycleOwner](https://developer.android.com/reference/kotlin/androidx/lifecycle/LifecycleOwner.html), vararg eventTypes: [Class](https://developer.android.com/reference/kotlin/java/lang/Class.html)&lt;out [ChatEvent](../../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt;, listener: (event: [ChatEvent](../../io.getstream.chat.android.client.events/ChatEvent/index.md)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)): [Disposable](../../io.getstream.chat.android.client.utils.observable/Disposable/index.md)  
More info  


Subscribes to the specific [eventTypes](subscribeFor.md) of the client, in the lifecycle of [lifecycleOwner](subscribeFor.md).



Only receives events when the lifecycle is in a STARTED state, otherwise events are dropped.



#### Since Kotlin  
99999.9  
  
  


[androidJvm]  
Content  
fun [subscribeFor](subscribeFor.md)(lifecycleOwner: [LifecycleOwner](https://developer.android.com/reference/kotlin/androidx/lifecycle/LifecycleOwner.html), vararg eventTypes: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), listener: [ChatEventListener](../ChatEventListener/index.md)&lt;[ChatEvent](../../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt;): [Disposable](../../io.getstream.chat.android.client.utils.observable/Disposable/index.md)  
fun [subscribeFor](subscribeFor.md)(lifecycleOwner: [LifecycleOwner](https://developer.android.com/reference/kotlin/androidx/lifecycle/LifecycleOwner.html), vararg eventTypes: [Class](https://developer.android.com/reference/kotlin/java/lang/Class.html)&lt;out [ChatEvent](../../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt;, listener: [ChatEventListener](../ChatEventListener/index.md)&lt;[ChatEvent](../../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt;): [Disposable](../../io.getstream.chat.android.client.utils.observable/Disposable/index.md)  
More info  


Subscribes to the specific [eventTypes](subscribeFor.md) of the client, in the lifecycle of [lifecycleOwner](subscribeFor.md).



Only receives events when the lifecycle is in a STARTED state, otherwise events are dropped.

  


[androidJvm]  
Content  
fun [subscribeFor](subscribeFor.md)(vararg eventTypes: [Class](https://developer.android.com/reference/kotlin/java/lang/Class.html)&lt;out [ChatEvent](../../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt;, listener: (event: [ChatEvent](../../io.getstream.chat.android.client.events/ChatEvent/index.md)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)): [Disposable](../../io.getstream.chat.android.client.utils.observable/Disposable/index.md)  
More info  


Subscribes to the specific [eventTypes](subscribeFor.md) of the client.



#### Since Kotlin  
99999.9  
  
  


[androidJvm]  
Content  
fun [subscribeFor](subscribeFor.md)(vararg eventTypes: [Class](https://developer.android.com/reference/kotlin/java/lang/Class.html)&lt;out [ChatEvent](../../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt;, listener: [ChatEventListener](../ChatEventListener/index.md)&lt;[ChatEvent](../../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt;): [Disposable](../../io.getstream.chat.android.client.utils.observable/Disposable/index.md)  
More info  


Subscribes to the specific [eventTypes](subscribeFor.md) of the client.

  



