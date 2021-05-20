---
title: queryChannelsLoadMore
---
/[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.offline](../index.md)/[ChatDomain](index.md)/[queryChannelsLoadMore](queryChannelsLoadMore.md)  
  
  
  
# queryChannelsLoadMore  
abstract fun [queryChannelsLoadMore](queryChannelsLoadMore.md)(filter: FilterObject, sort: QuerySort&lt;Channel&gt;, limit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), messageLimit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): Call&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;Channel&gt;&gt;Load more channels for this query  
  
#### Return  
executable async Call responsible for loading more channels  
  
## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>io.getstream.chat.android.client.api.models.FilterObject| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>|
| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>io.getstream.chat.android.client.api.models.QuerySort| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>|
| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>&lt;a href="https://getstream.io/chat/docs/query_channels/?language=kotlin"&gt;Filter syntax&lt;/a&gt;|
  
  
  
## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>filter| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>the filter for querying channels, see https://getstream.io/chat/docs/query_channels/?language=kotlin|
| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>sort| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>the sort for the channels, by default will sort on last_message_at|
| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>limit| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>the number of channels to retrieve|
| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>messageLimit| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>how many messages to fetch per chanel|
  
abstract fun [queryChannelsLoadMore](queryChannelsLoadMore.md)(filter: FilterObject, sort: QuerySort&lt;Channel&gt;, messageLimit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): Call&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;Channel&gt;&gt;Load more channels for this query  
  
#### Return  
executable async Call responsible for loading more channels  
  
## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int/PointingToDeclaration/"></a>io.getstream.chat.android.client.api.models.FilterObject| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int/PointingToDeclaration/"></a>|
| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int/PointingToDeclaration/"></a>io.getstream.chat.android.client.api.models.QuerySort| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int/PointingToDeclaration/"></a>|
| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int/PointingToDeclaration/"></a>| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int/PointingToDeclaration/"></a>&lt;a href="https://getstream.io/chat/docs/query_channels/?language=kotlin"&gt;Filter syntax&lt;/a&gt;|
  
  
  
## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int/PointingToDeclaration/"></a>filter| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int/PointingToDeclaration/"></a>the filter for querying channels, see https://getstream.io/chat/docs/query_channels/?language=kotlin|
| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int/PointingToDeclaration/"></a>sort| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int/PointingToDeclaration/"></a>the sort for the channels, by default will sort on last_message_at|
| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int/PointingToDeclaration/"></a>messageLimit| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int/PointingToDeclaration/"></a>how many messages to fetch per chanel|
  
abstract fun [queryChannelsLoadMore](queryChannelsLoadMore.md)(filter: FilterObject, sort: QuerySort&lt;Channel&gt;): Call&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;Channel&gt;&gt;Load more channels for this query  
  
#### Return  
executable async Call responsible for loading more channels  
  
## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]/PointingToDeclaration/"></a>io.getstream.chat.android.client.api.models.FilterObject| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]/PointingToDeclaration/"></a>|
| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]/PointingToDeclaration/"></a>io.getstream.chat.android.client.api.models.QuerySort| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]/PointingToDeclaration/"></a>|
| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]/PointingToDeclaration/"></a>| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]/PointingToDeclaration/"></a>&lt;a href="https://getstream.io/chat/docs/query_channels/?language=kotlin"&gt;Filter syntax&lt;/a&gt;|
  
  
  
## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]/PointingToDeclaration/"></a>filter| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]/PointingToDeclaration/"></a>the filter for querying channels, see https://getstream.io/chat/docs/query_channels/?language=kotlin|
| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]/PointingToDeclaration/"></a>sort| <a name="io.getstream.chat.android.offline/ChatDomain/queryChannelsLoadMore/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]/PointingToDeclaration/"></a>the sort for the channels, by default will sort on last_message_at|
  

