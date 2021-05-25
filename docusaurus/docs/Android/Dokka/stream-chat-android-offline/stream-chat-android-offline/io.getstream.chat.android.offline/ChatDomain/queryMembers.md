---
title: queryMembers
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.offline](../index.md)/[ChatDomain](index.md)/[queryMembers](queryMembers.md)



# queryMembers  
[androidJvm]  
Content  
abstract fun [queryMembers](queryMembers.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), offset: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0, limit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0, filter: FilterObject = NeutralFilterObject, sort: QuerySort&lt;Member&gt; = QuerySort.desc(Member::createdAt), members: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;Member&gt; = emptyList()): Call&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;Member&gt;&gt;  
More info  


Query members of a channel



#### Return  


executable async Call querying members



## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.offline/ChatDomain/queryMembers/#kotlin.String#kotlin.Int#kotlin.Int#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Member]#kotlin.collections.List[io.getstream.chat.android.client.models.Member]/PointingToDeclaration/"></a>cid| <a name="io.getstream.chat.android.offline/ChatDomain/queryMembers/#kotlin.String#kotlin.Int#kotlin.Int#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Member]#kotlin.collections.List[io.getstream.chat.android.client.models.Member]/PointingToDeclaration/"></a><br/><br/>CID of the Channel whose members we are querying<br/><br/>|
| <a name="io.getstream.chat.android.offline/ChatDomain/queryMembers/#kotlin.String#kotlin.Int#kotlin.Int#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Member]#kotlin.collections.List[io.getstream.chat.android.client.models.Member]/PointingToDeclaration/"></a>offset| <a name="io.getstream.chat.android.offline/ChatDomain/queryMembers/#kotlin.String#kotlin.Int#kotlin.Int#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Member]#kotlin.collections.List[io.getstream.chat.android.client.models.Member]/PointingToDeclaration/"></a><br/><br/>indicates how many items to exclude from the start of the result<br/><br/>|
| <a name="io.getstream.chat.android.offline/ChatDomain/queryMembers/#kotlin.String#kotlin.Int#kotlin.Int#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Member]#kotlin.collections.List[io.getstream.chat.android.client.models.Member]/PointingToDeclaration/"></a>limit| <a name="io.getstream.chat.android.offline/ChatDomain/queryMembers/#kotlin.String#kotlin.Int#kotlin.Int#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Member]#kotlin.collections.List[io.getstream.chat.android.client.models.Member]/PointingToDeclaration/"></a><br/><br/>indicates the maximum allowed number of items in the result<br/><br/>|
| <a name="io.getstream.chat.android.offline/ChatDomain/queryMembers/#kotlin.String#kotlin.Int#kotlin.Int#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Member]#kotlin.collections.List[io.getstream.chat.android.client.models.Member]/PointingToDeclaration/"></a>filter| <a name="io.getstream.chat.android.offline/ChatDomain/queryMembers/#kotlin.String#kotlin.Int#kotlin.Int#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Member]#kotlin.collections.List[io.getstream.chat.android.client.models.Member]/PointingToDeclaration/"></a><br/><br/>applied to online queries for advanced selection criteria<br/><br/>|
| <a name="io.getstream.chat.android.offline/ChatDomain/queryMembers/#kotlin.String#kotlin.Int#kotlin.Int#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Member]#kotlin.collections.List[io.getstream.chat.android.client.models.Member]/PointingToDeclaration/"></a>sort| <a name="io.getstream.chat.android.offline/ChatDomain/queryMembers/#kotlin.String#kotlin.Int#kotlin.Int#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Member]#kotlin.collections.List[io.getstream.chat.android.client.models.Member]/PointingToDeclaration/"></a><br/><br/>the sort criteria applied to the result<br/><br/>|
| <a name="io.getstream.chat.android.offline/ChatDomain/queryMembers/#kotlin.String#kotlin.Int#kotlin.Int#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Member]#kotlin.collections.List[io.getstream.chat.android.client.models.Member]/PointingToDeclaration/"></a>members| <a name="io.getstream.chat.android.offline/ChatDomain/queryMembers/#kotlin.String#kotlin.Int#kotlin.Int#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Member]#kotlin.collections.List[io.getstream.chat.android.client.models.Member]/PointingToDeclaration/"></a>|
  
  



