---
title: getThread
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.livedata](../index.md)/[ChatDomain](index.md)/[getThread](getThread.md)



# getThread  
[androidJvm]  
Content  
abstract fun [getThread](getThread.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), parentId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): Call&lt;[ThreadController](../../io.getstream.chat.android.livedata.controller/ThreadController/index.md)&gt;  
More info  


Returns a thread controller for the given channel and message id



#### Return  


executable async Call responsible for obtaining [ThreadController](../../io.getstream.chat.android.livedata.controller/ThreadController/index.md)



## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata/ChatDomain/getThread/#kotlin.String#kotlin.String/PointingToDeclaration/"></a>[io.getstream.chat.android.livedata.controller.ThreadController](../../io.getstream.chat.android.livedata.controller/ThreadController/index.md)| <a name="io.getstream.chat.android.livedata/ChatDomain/getThread/#kotlin.String#kotlin.String/PointingToDeclaration/"></a>|
  


## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata/ChatDomain/getThread/#kotlin.String#kotlin.String/PointingToDeclaration/"></a>cid| <a name="io.getstream.chat.android.livedata/ChatDomain/getThread/#kotlin.String#kotlin.String/PointingToDeclaration/"></a><br/><br/>the full channel id. ie messaging:123<br/><br/>|
| <a name="io.getstream.chat.android.livedata/ChatDomain/getThread/#kotlin.String#kotlin.String/PointingToDeclaration/"></a>parentId| <a name="io.getstream.chat.android.livedata/ChatDomain/getThread/#kotlin.String#kotlin.String/PointingToDeclaration/"></a><br/><br/>the message id for the parent of this thread<br/><br/>|
  
  



