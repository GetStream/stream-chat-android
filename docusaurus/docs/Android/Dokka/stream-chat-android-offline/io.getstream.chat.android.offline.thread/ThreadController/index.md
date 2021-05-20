---
title: index
sidebar_position: 1
---
/[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.offline.thread](../index.md)/[ThreadController](index.md)  
  
  
  
# ThreadController  
class [ThreadController](index.md)  
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.offline.thread/ThreadController/endOfOlderMessages/#/PointingToDeclaration/"></a>[endOfOlderMessages](endOfOlderMessages.md)| <a name="io.getstream.chat.android.offline.thread/ThreadController/endOfOlderMessages/#/PointingToDeclaration/"></a>val [endOfOlderMessages](endOfOlderMessages.md): StateFlow&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;if we've reached the earliest point in this thread|
| <a name="io.getstream.chat.android.offline.thread/ThreadController/loadingOlderMessages/#/PointingToDeclaration/"></a>[loadingOlderMessages](loadingOlderMessages.md)| <a name="io.getstream.chat.android.offline.thread/ThreadController/loadingOlderMessages/#/PointingToDeclaration/"></a>val [loadingOlderMessages](loadingOlderMessages.md): StateFlow&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;if we are currently loading older messages|
| <a name="io.getstream.chat.android.offline.thread/ThreadController/messages/#/PointingToDeclaration/"></a>[messages](messages.md)| <a name="io.getstream.chat.android.offline.thread/ThreadController/messages/#/PointingToDeclaration/"></a>val [messages](messages.md): StateFlow&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;Message&gt;&gt;the sorted list of messages for this thread|
| <a name="io.getstream.chat.android.offline.thread/ThreadController/threadId/#/PointingToDeclaration/"></a>[threadId](threadId.md)| <a name="io.getstream.chat.android.offline.thread/ThreadController/threadId/#/PointingToDeclaration/"></a>val [threadId](threadId.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)|
  
  
## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.offline.thread/ThreadController/getMessagesSorted/#/PointingToDeclaration/"></a>[getMessagesSorted](getMessagesSorted.md)| <a name="io.getstream.chat.android.offline.thread/ThreadController/getMessagesSorted/#/PointingToDeclaration/"></a>fun [getMessagesSorted](getMessagesSorted.md)(): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;Message&gt;|

