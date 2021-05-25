---
title: index
sidebar_position: 1
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.livedata.controller](../index.md)/[ThreadController](index.md)



# ThreadController  
 [androidJvm] interface [ThreadController](index.md)

The threadController exposes livedata for a thread

<ul><li>threadId (the id of the current thread)</li><li>loadingOlderMessages (if we're currently loading older messages)</li><li>endOfOlderMessages (if you've reached the end of older messages)</li></ul>   


## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.livedata.controller/ThreadController/endOfOlderMessages/#/PointingToDeclaration/"></a>[endOfOlderMessages](endOfOlderMessages.md)| <a name="io.getstream.chat.android.livedata.controller/ThreadController/endOfOlderMessages/#/PointingToDeclaration/"></a> [androidJvm] abstract val [endOfOlderMessages](endOfOlderMessages.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;   <br/>|
| <a name="io.getstream.chat.android.livedata.controller/ThreadController/loadingOlderMessages/#/PointingToDeclaration/"></a>[loadingOlderMessages](loadingOlderMessages.md)| <a name="io.getstream.chat.android.livedata.controller/ThreadController/loadingOlderMessages/#/PointingToDeclaration/"></a> [androidJvm] abstract val [loadingOlderMessages](loadingOlderMessages.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;   <br/>|
| <a name="io.getstream.chat.android.livedata.controller/ThreadController/messages/#/PointingToDeclaration/"></a>[messages](messages.md)| <a name="io.getstream.chat.android.livedata.controller/ThreadController/messages/#/PointingToDeclaration/"></a> [androidJvm] abstract val [messages](messages.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;Message&gt;&gt;   <br/>|
| <a name="io.getstream.chat.android.livedata.controller/ThreadController/threadId/#/PointingToDeclaration/"></a>[threadId](threadId.md)| <a name="io.getstream.chat.android.livedata.controller/ThreadController/threadId/#/PointingToDeclaration/"></a> [androidJvm] abstract val [threadId](threadId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br/>|


## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.livedata.controller/ThreadController/getMessagesSorted/#/PointingToDeclaration/"></a>[getMessagesSorted](getMessagesSorted.md)| <a name="io.getstream.chat.android.livedata.controller/ThreadController/getMessagesSorted/#/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>abstract fun [getMessagesSorted](getMessagesSorted.md)(): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;Message&gt;  <br/><br/><br/>|

