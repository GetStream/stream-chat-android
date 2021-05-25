---
title: index
sidebar_position: 1
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.livedata.utils](../index.md)/[EventObserver](index.md)



# EventObserver  
 [androidJvm] class [EventObserver](index.md)&lt;[T](index.md)&gt;(**onEventUnhandledContent**: ([T](index.md)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)) : [Observer](https://developer.android.com/reference/kotlin/androidx/lifecycle/Observer.html)&lt;[Event](../Event/index.md)&lt;[T](index.md)&gt;&gt; 

An [Observer](https://developer.android.com/reference/kotlin/androidx/lifecycle/Observer.html) for [Event](../Event/index.md)s, simplifying the pattern of checking if the [Event](../Event/index.md)'s content has already been handled.



onEventUnhandledContent is *only* called if the [Event](../Event/index.md)'s contents has not been handled.

   


## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata.utils/EventObserver/EventObserver/#kotlin.Function1[TypeParam(bounds=[kotlin.Any?]),kotlin.Unit]/PointingToDeclaration/"></a>[EventObserver](EventObserver.md)| <a name="io.getstream.chat.android.livedata.utils/EventObserver/EventObserver/#kotlin.Function1[TypeParam(bounds=[kotlin.Any?]),kotlin.Unit]/PointingToDeclaration/"></a> [androidJvm] fun &lt;[T](index.md)&gt; [EventObserver](EventObserver.md)(onEventUnhandledContent: ([T](index.md)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))   <br/>|


## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.livedata.utils/EventObserver/onChanged/#io.getstream.chat.android.livedata.utils.Event[TypeParam(bounds=[kotlin.Any?])]?/PointingToDeclaration/"></a>[onChanged](onChanged.md)| <a name="io.getstream.chat.android.livedata.utils/EventObserver/onChanged/#io.getstream.chat.android.livedata.utils.Event[TypeParam(bounds=[kotlin.Any?])]?/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>open override fun [onChanged](onChanged.md)(event: [Event](../Event/index.md)&lt;[T](index.md)&gt;?)  <br/><br/><br/>|

