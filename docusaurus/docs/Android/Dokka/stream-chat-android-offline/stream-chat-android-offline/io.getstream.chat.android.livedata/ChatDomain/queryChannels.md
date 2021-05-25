---
title: queryChannels
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.livedata](../index.md)/[ChatDomain](index.md)/[queryChannels](queryChannels.md)



# queryChannels  
[androidJvm]  
Content  
abstract fun [queryChannels](queryChannels.md)(filter: FilterObject, sort: QuerySort&lt;Channel&gt;, limit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 30, messageLimit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 1): Call&lt;[QueryChannelsController](../../io.getstream.chat.android.livedata.controller/QueryChannelsController/index.md)&gt;  
More info  


Queries offline storage and the API for channels matching the filter Returns a queryChannelsController



#### Return  


executable async Call responsible for obtaining [QueryChannelsController](../../io.getstream.chat.android.livedata.controller/QueryChannelsController/index.md)



## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata/ChatDomain/queryChannels/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>[io.getstream.chat.android.livedata.controller.QueryChannelsController](../../io.getstream.chat.android.livedata.controller/QueryChannelsController/index.md)| <a name="io.getstream.chat.android.livedata/ChatDomain/queryChannels/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>|
| <a name="io.getstream.chat.android.livedata/ChatDomain/queryChannels/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>io.getstream.chat.android.client.utils.FilterObject| <a name="io.getstream.chat.android.livedata/ChatDomain/queryChannels/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>|
| <a name="io.getstream.chat.android.livedata/ChatDomain/queryChannels/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>io.getstream.chat.android.client.api.models.QuerySort| <a name="io.getstream.chat.android.livedata/ChatDomain/queryChannels/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>|
| <a name="io.getstream.chat.android.livedata/ChatDomain/queryChannels/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>| <a name="io.getstream.chat.android.livedata/ChatDomain/queryChannels/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a><br/><br/>&lt;a href="https://getstream.io/chat/docs/query_channels/?language=kotlin"&gt;Filter syntax&lt;/a&gt;<br/><br/>|
  


## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata/ChatDomain/queryChannels/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>filter| <a name="io.getstream.chat.android.livedata/ChatDomain/queryChannels/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a><br/><br/>the filter object<br/><br/>|
| <a name="io.getstream.chat.android.livedata/ChatDomain/queryChannels/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>sort| <a name="io.getstream.chat.android.livedata/ChatDomain/queryChannels/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a><br/><br/>how to sort the channels (default is last_message_at)<br/><br/>|
| <a name="io.getstream.chat.android.livedata/ChatDomain/queryChannels/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>limit| <a name="io.getstream.chat.android.livedata/ChatDomain/queryChannels/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a><br/><br/>the number of channels to retrieve<br/><br/>|
| <a name="io.getstream.chat.android.livedata/ChatDomain/queryChannels/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>messageLimit| <a name="io.getstream.chat.android.livedata/ChatDomain/queryChannels/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a><br/><br/>how many messages to retrieve per channel<br/><br/>|
  
  



