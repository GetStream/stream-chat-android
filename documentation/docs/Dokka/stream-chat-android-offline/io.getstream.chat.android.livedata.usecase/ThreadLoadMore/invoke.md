---
title: invoke
---
/[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.livedata.usecase](../index.md)/[ThreadLoadMore](index.md)/[invoke](invoke.md)  
  
  
  
# invoke  
@[CheckResult](https://developer.android.com/reference/kotlin/androidx/annotation/CheckResult.html)()abstract operator fun [invoke](invoke.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), parentId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), messageLimit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): Call&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;Message&gt;&gt;Loads more messages for the specified thread  
  
## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/ThreadLoadMore/invoke/#kotlin.String#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>cid| <a name="io.getstream.chat.android.livedata.usecase/ThreadLoadMore/invoke/#kotlin.String#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>: the full channel id i. e. messaging:123|
| <a name="io.getstream.chat.android.livedata.usecase/ThreadLoadMore/invoke/#kotlin.String#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>parentId| <a name="io.getstream.chat.android.livedata.usecase/ThreadLoadMore/invoke/#kotlin.String#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>: the parentId of the thread|
| <a name="io.getstream.chat.android.livedata.usecase/ThreadLoadMore/invoke/#kotlin.String#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>messageLimit| <a name="io.getstream.chat.android.livedata.usecase/ThreadLoadMore/invoke/#kotlin.String#kotlin.String#kotlin.Int/PointingToDeclaration/"></a>: how many new messages to load|
  

