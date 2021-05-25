---
title: index
sidebar_position: 1
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.livedata.utils](../index.md)/[Event](index.md)



# Event  
 [androidJvm] open class [Event](index.md)&lt;out [T](index.md)&gt;

Used as a wrapper for data that represents an event.

   


## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata.utils/Event/Event/#TypeParam(bounds=[kotlin.Any?])/PointingToDeclaration/"></a>[Event](Event.md)| <a name="io.getstream.chat.android.livedata.utils/Event/Event/#TypeParam(bounds=[kotlin.Any?])/PointingToDeclaration/"></a> [androidJvm] fun &lt;out [T](index.md)&gt; [Event](Event.md)(content: [T](index.md))   <br/>|


## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.livedata.utils/Event/hasBeenHandled/#/PointingToDeclaration/"></a>[hasBeenHandled](hasBeenHandled.md)| <a name="io.getstream.chat.android.livedata.utils/Event/hasBeenHandled/#/PointingToDeclaration/"></a> [androidJvm] val [hasBeenHandled](hasBeenHandled.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)   <br/>|


## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.livedata.utils/Event/getContentIfNotHandled/#/PointingToDeclaration/"></a>[getContentIfNotHandled](getContentIfNotHandled.md)| <a name="io.getstream.chat.android.livedata.utils/Event/getContentIfNotHandled/#/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>fun [getContentIfNotHandled](getContentIfNotHandled.md)(): [T](index.md)?  <br/>More info  <br/>Returns the content and prevents its use again.  <br/><br/><br/>|
| <a name="io.getstream.chat.android.livedata.utils/Event/peekContent/#/PointingToDeclaration/"></a>[peekContent](peekContent.md)| <a name="io.getstream.chat.android.livedata.utils/Event/peekContent/#/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>fun [peekContent](peekContent.md)(): [T](index.md)  <br/>More info  <br/>Returns the content, even if it's already been handled.  <br/><br/><br/>|

