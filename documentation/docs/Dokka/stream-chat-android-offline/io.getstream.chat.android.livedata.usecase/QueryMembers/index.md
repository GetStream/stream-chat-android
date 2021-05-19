---
title: index
sidebar_position: 1
---
/[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.livedata.usecase](../index.md)/[QueryMembers](index.md)  
  
  
  
# QueryMembers  
class [QueryMembers](index.md)UseCase for querying members of a channel  
  
## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/QueryMembers/invoke/#kotlin.String#kotlin.Int#kotlin.Int#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Member]#kotlin.collections.List[io.getstream.chat.android.client.models.Member]/PointingToDeclaration/"></a>[invoke](invoke.md)| <a name="io.getstream.chat.android.livedata.usecase/QueryMembers/invoke/#kotlin.String#kotlin.Int#kotlin.Int#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Member]#kotlin.collections.List[io.getstream.chat.android.client.models.Member]/PointingToDeclaration/"></a>operator fun [invoke](invoke.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), offset: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0, limit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0, filter: FilterObject = NeutralFilterObject, sort: QuerySort&lt;Member&gt; = QuerySort.desc(Member::createdAt), members: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;Member&gt; = emptyList()): Call&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;Member&gt;&gt;Obtains an executable coroutine call for querying members|

