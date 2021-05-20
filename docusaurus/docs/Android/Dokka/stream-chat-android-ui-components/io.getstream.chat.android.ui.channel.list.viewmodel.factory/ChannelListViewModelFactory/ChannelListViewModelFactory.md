---
title: ChannelListViewModelFactory
---
/[stream-chat-android-ui-components](../../index.md)/[io.getstream.chat.android.ui.channel.list.viewmodel.factory](../index.md)/[ChannelListViewModelFactory](index.md)/[ChannelListViewModelFactory](ChannelListViewModelFactory.md)  
  
  
  
# ChannelListViewModelFactory  
@[JvmOverloads](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-overloads/index.html)()fun [ChannelListViewModelFactory](ChannelListViewModelFactory.md)(filter: FilterObject = Filters.and(
        Filters.eq("type", "messaging"),
        Filters.`in`("members", listOf(ChatDomain.instance().currentUser.id)),
        Filters.or(Filters.notExists("draft"), Filters.ne("draft", true)),
    ), sort: QuerySort&lt;Channel&gt; = ChannelListViewModel.DEFAULT_SORT, limit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 30, messageLimit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 1)  
  
## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel.factory/ChannelListViewModelFactory/ChannelListViewModelFactory/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>filter| <a name="io.getstream.chat.android.ui.channel.list.viewmodel.factory/ChannelListViewModelFactory/ChannelListViewModelFactory/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>how to filter the channels|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel.factory/ChannelListViewModelFactory/ChannelListViewModelFactory/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>sort| <a name="io.getstream.chat.android.ui.channel.list.viewmodel.factory/ChannelListViewModelFactory/ChannelListViewModelFactory/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>how to sort the channels, defaults to last_updated|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel.factory/ChannelListViewModelFactory/ChannelListViewModelFactory/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>limit| <a name="io.getstream.chat.android.ui.channel.list.viewmodel.factory/ChannelListViewModelFactory/ChannelListViewModelFactory/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>how many channels to return|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel.factory/ChannelListViewModelFactory/ChannelListViewModelFactory/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>messageLimit| <a name="io.getstream.chat.android.ui.channel.list.viewmodel.factory/ChannelListViewModelFactory/ChannelListViewModelFactory/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>the number of messages to fetch for each channel|
  

