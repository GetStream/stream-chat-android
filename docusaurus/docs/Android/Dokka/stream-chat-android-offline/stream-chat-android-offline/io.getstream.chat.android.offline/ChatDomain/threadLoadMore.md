---
title: threadLoadMore
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.offline](../index.md)/[ChatDomain](index.md)/[threadLoadMore](threadLoadMore.md)



# threadLoadMore  
[androidJvm]  
Content  
abstract fun [threadLoadMore](threadLoadMore.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), parentId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), messageLimit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): Call&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;Message&gt;&gt;  
More info  


Loads more messages for the specified thread



#### Return  


executable async Call responsible for loading more messages in a thread



## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.offline/ChatDomain/threadLoadMore/#kotlin.String#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>cid| <a name="io.getstream.chat.android.offline/ChatDomain/threadLoadMore/#kotlin.String#kotlin.String#kotlin.Int/PointingToDeclaration/"></a><br/><br/>: the full channel id i. e. messaging:123<br/><br/>|
| <a name="io.getstream.chat.android.offline/ChatDomain/threadLoadMore/#kotlin.String#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>parentId| <a name="io.getstream.chat.android.offline/ChatDomain/threadLoadMore/#kotlin.String#kotlin.String#kotlin.Int/PointingToDeclaration/"></a><br/><br/>: the parentId of the thread<br/><br/>|
| <a name="io.getstream.chat.android.offline/ChatDomain/threadLoadMore/#kotlin.String#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>messageLimit| <a name="io.getstream.chat.android.offline/ChatDomain/threadLoadMore/#kotlin.String#kotlin.String#kotlin.Int/PointingToDeclaration/"></a><br/><br/>: how many new messages to load<br/><br/>|
  
  



