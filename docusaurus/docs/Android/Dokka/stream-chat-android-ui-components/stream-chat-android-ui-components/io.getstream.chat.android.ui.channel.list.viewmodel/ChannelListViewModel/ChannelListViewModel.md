---
title: ChannelListViewModel
---
//[stream-chat-android-ui-components](../../../index.md)/[io.getstream.chat.android.ui.channel.list.viewmodel](../index.md)/[ChannelListViewModel](index.md)/[ChannelListViewModel](ChannelListViewModel.md)



# ChannelListViewModel  
[androidJvm]  
Content  
fun [ChannelListViewModel](ChannelListViewModel.md)(chatDomain: ChatDomain = ChatDomain.instance(), filter: FilterObject = Filters.and(
        Filters.eq("type", "messaging"),
        Filters.`in`("members", listOf(chatDomain.currentUser.id)),
        Filters.or(Filters.notExists("draft"), Filters.ne("draft", true)),
    ), sort: QuerySort&lt;Channel&gt; = DEFAULT_SORT, limit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 30, messageLimit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 1)  
More info  


## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/ChannelListViewModel/#io.getstream.chat.android.livedata.ChatDomain#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>chatDomain| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/ChannelListViewModel/#io.getstream.chat.android.livedata.ChatDomain#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a><br/><br/>entry point for all livedata & offline operations<br/><br/>|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/ChannelListViewModel/#io.getstream.chat.android.livedata.ChatDomain#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>filter| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/ChannelListViewModel/#io.getstream.chat.android.livedata.ChatDomain#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a><br/><br/>filter for querying channels, should never be empty<br/><br/>|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/ChannelListViewModel/#io.getstream.chat.android.livedata.ChatDomain#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>sort| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/ChannelListViewModel/#io.getstream.chat.android.livedata.ChatDomain#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a><br/><br/>defines the ordering of the channels<br/><br/>|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/ChannelListViewModel/#io.getstream.chat.android.livedata.ChatDomain#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>limit| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/ChannelListViewModel/#io.getstream.chat.android.livedata.ChatDomain#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a><br/><br/>the maximum number of channels to fetch<br/><br/>|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/ChannelListViewModel/#io.getstream.chat.android.livedata.ChatDomain#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>messageLimit| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/ChannelListViewModel/#io.getstream.chat.android.livedata.ChatDomain#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a><br/><br/>the number of messages to fetch for each channel<br/><br/>|
  
  



