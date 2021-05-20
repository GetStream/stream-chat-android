---
title: index
sidebar_position: 1
---
/[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.offline.utils](../index.md)/[Event](index.md)  
  
  
  
# Event  
open class [Event](index.md)&lt;out [T](index.md)&gt;(**content**: [T](index.md))Used as a wrapper for data that represents an event.  
  
## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.offline.utils/Event/Event/#TypeParam(bounds=[kotlin.Any?])/PointingToDeclaration/"></a>[Event](Event.md)| <a name="io.getstream.chat.android.offline.utils/Event/Event/#TypeParam(bounds=[kotlin.Any?])/PointingToDeclaration/"></a>fun &lt;out [T](index.md)&gt; [Event](Event.md)(content: [T](index.md))|
  
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.offline.utils/Event/hasBeenHandled/#/PointingToDeclaration/"></a>[hasBeenHandled](hasBeenHandled.md)| <a name="io.getstream.chat.android.offline.utils/Event/hasBeenHandled/#/PointingToDeclaration/"></a>var [hasBeenHandled](hasBeenHandled.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false|
  
  
## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.offline.utils/Event/getContentIfNotHandled/#/PointingToDeclaration/"></a>[getContentIfNotHandled](getContentIfNotHandled.md)| <a name="io.getstream.chat.android.offline.utils/Event/getContentIfNotHandled/#/PointingToDeclaration/"></a>fun [getContentIfNotHandled](getContentIfNotHandled.md)(): [T](index.md)?Returns the content and prevents its use again.|
| <a name="io.getstream.chat.android.offline.utils/Event/peekContent/#/PointingToDeclaration/"></a>[peekContent](peekContent.md)| <a name="io.getstream.chat.android.offline.utils/Event/peekContent/#/PointingToDeclaration/"></a>fun [peekContent](peekContent.md)(): [T](index.md)Returns the content, even if it's already been handled.|

