---
title: subscribeForSingle
---
//[stream-chat-android-client](../../../index.md)/[io.getstream.chat.android.client](../index.md)/[ChatClient](index.md)/[subscribeForSingle](subscribeForSingle.md)



# subscribeForSingle  
[androidJvm]  
Content  
fun [subscribeForSingle](subscribeForSingle.md)(eventType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), listener: (event: [ChatEvent](../../io.getstream.chat.android.client.events/ChatEvent/index.md)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)): [Disposable](../../io.getstream.chat.android.client.utils.observable/Disposable/index.md)  
fun &lt;[T](subscribeForSingle.md) : [ChatEvent](../../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt; [subscribeForSingle](subscribeForSingle.md)(eventType: [Class](https://developer.android.com/reference/kotlin/java/lang/Class.html)&lt;[T](subscribeForSingle.md)&gt;, listener: ([T](subscribeForSingle.md)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)): [Disposable](../../io.getstream.chat.android.client.utils.observable/Disposable/index.md)  
More info  


Subscribes for the next event with the given [eventType](subscribeForSingle.md).



#### Since Kotlin  
99999.9  
  
  


[androidJvm]  
Content  
fun [subscribeForSingle](subscribeForSingle.md)(eventType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), listener: [ChatEventListener](../ChatEventListener/index.md)&lt;[ChatEvent](../../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt;): [Disposable](../../io.getstream.chat.android.client.utils.observable/Disposable/index.md)  
fun &lt;[T](subscribeForSingle.md) : [ChatEvent](../../io.getstream.chat.android.client.events/ChatEvent/index.md)&gt; [subscribeForSingle](subscribeForSingle.md)(eventType: [Class](https://developer.android.com/reference/kotlin/java/lang/Class.html)&lt;[T](subscribeForSingle.md)&gt;, listener: [ChatEventListener](../ChatEventListener/index.md)&lt;[T](subscribeForSingle.md)&gt;): [Disposable](../../io.getstream.chat.android.client.utils.observable/Disposable/index.md)  
More info  


Subscribes for the next event with the given [eventType](subscribeForSingle.md).

  



