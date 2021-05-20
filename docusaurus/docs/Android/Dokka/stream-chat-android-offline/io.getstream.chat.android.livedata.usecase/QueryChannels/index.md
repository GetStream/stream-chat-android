---
title: index
sidebar_position: 1
---
/[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.livedata.usecase](../index.md)/[QueryChannels](index.md)  
  
  
  
# QueryChannels  
interface [QueryChannels](index.md)  
  
## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/QueryChannels/invoke/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>[invoke](invoke.md)| <a name="io.getstream.chat.android.livedata.usecase/QueryChannels/invoke/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>@[CheckResult](https://developer.android.com/reference/kotlin/androidx/annotation/CheckResult.html)()abstract operator fun [invoke](invoke.md)(filter: FilterObject, sort: QuerySort&lt;Channel&gt;, limit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 30, messageLimit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 1): Call&lt;[QueryChannelsController](../../io.getstream.chat.android.livedata.controller/QueryChannelsController/index.md)&gt;Queries offline storage and the API for channels matching the filter Returns a queryChannelsController|

