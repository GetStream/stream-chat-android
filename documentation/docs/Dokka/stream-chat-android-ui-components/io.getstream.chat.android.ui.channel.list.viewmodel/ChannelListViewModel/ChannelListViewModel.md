---
title: ChannelListViewModel
---
/[stream-chat-android-ui-components](../../index.md)/[io.getstream.chat.android.ui.channel.list.viewmodel](../index.md)/[ChannelListViewModel](index.md)/[ChannelListViewModel](ChannelListViewModel.md)  
  
  
  
# ChannelListViewModel  
fun [ChannelListViewModel](ChannelListViewModel.md)(chatDomain: ChatDomain = ChatDomain.instance(), filter: FilterObject = Filters.and(
        eq("type", "messaging"),
        `in`("members", listOf(chatDomain.currentUser.id)),
        or(Filters.notExists("draft"), ne("draft", true)),
        or(Filters.notExists("hidden"), ne("hidden", true)),
    ), sort: QuerySort&lt;Channel&gt; = DEFAULT_SORT, limit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 30, messageLimit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 1)  
  
## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/ChannelListViewModel/#io.getstream.chat.android.livedata.ChatDomain#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>chatDomain| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/ChannelListViewModel/#io.getstream.chat.android.livedata.ChatDomain#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>entry point for all livedata & offline operations|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/ChannelListViewModel/#io.getstream.chat.android.livedata.ChatDomain#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>filter| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/ChannelListViewModel/#io.getstream.chat.android.livedata.ChatDomain#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>filter for querying channels, should never be empty|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/ChannelListViewModel/#io.getstream.chat.android.livedata.ChatDomain#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>sort| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/ChannelListViewModel/#io.getstream.chat.android.livedata.ChatDomain#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>defines the ordering of the channels|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/ChannelListViewModel/#io.getstream.chat.android.livedata.ChatDomain#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>limit| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/ChannelListViewModel/#io.getstream.chat.android.livedata.ChatDomain#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>the maximum number of channels to fetch|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/ChannelListViewModel/#io.getstream.chat.android.livedata.ChatDomain#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>messageLimit| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/ChannelListViewModel/#io.getstream.chat.android.livedata.ChatDomain#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>the number of messages to fetch for each channel|
  

