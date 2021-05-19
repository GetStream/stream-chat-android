---
title: threadLoadMore
---
/[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.livedata](../index.md)/[ChatDomain](index.md)/[threadLoadMore](threadLoadMore.md)  
  
  
  
# threadLoadMore  
abstract fun [threadLoadMore](threadLoadMore.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), parentId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), messageLimit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): Call&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;Message&gt;&gt;Loads more messages for the specified thread  
  
#### Return  
executable async Call responsible for loading more messages in a thread  
  
## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata/ChatDomain/threadLoadMore/#kotlin.String#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>cid| <a name="io.getstream.chat.android.livedata/ChatDomain/threadLoadMore/#kotlin.String#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>: the full channel id i. e. messaging:123|
| <a name="io.getstream.chat.android.livedata/ChatDomain/threadLoadMore/#kotlin.String#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>parentId| <a name="io.getstream.chat.android.livedata/ChatDomain/threadLoadMore/#kotlin.String#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>: the parentId of the thread|
| <a name="io.getstream.chat.android.livedata/ChatDomain/threadLoadMore/#kotlin.String#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>messageLimit| <a name="io.getstream.chat.android.livedata/ChatDomain/threadLoadMore/#kotlin.String#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>: how many new messages to load|
  

