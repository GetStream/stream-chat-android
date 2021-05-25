---
title: index
sidebar_position: 1
---
//[stream-chat-android-ui-components](../../../index.md)/[io.getstream.chat.android.ui.channel.list.viewmodel.factory](../index.md)/[ChannelListViewModelFactory](index.md)



# ChannelListViewModelFactory  
 [androidJvm] class [ChannelListViewModelFactory](index.md)@[JvmOverloads](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-overloads/index.html)()constructor(**filter**: FilterObject, **sort**: QuerySort&lt;Channel&gt;, **limit**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), **messageLimit**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) : [ViewModelProvider.Factory](https://developer.android.com/reference/kotlin/androidx/lifecycle/ViewModelProvider.Factory.html)

Creates a channels view model factory

   


## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel.factory/ChannelListViewModelFactory///PointingToDeclaration/"></a>io.getstream.chat.android.client.models.Filters| <a name="io.getstream.chat.android.ui.channel.list.viewmodel.factory/ChannelListViewModelFactory///PointingToDeclaration/"></a>|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel.factory/ChannelListViewModelFactory///PointingToDeclaration/"></a>io.getstream.chat.android.client.api.models.QuerySort| <a name="io.getstream.chat.android.ui.channel.list.viewmodel.factory/ChannelListViewModelFactory///PointingToDeclaration/"></a>|
  


## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel.factory/ChannelListViewModelFactory///PointingToDeclaration/"></a>filter| <a name="io.getstream.chat.android.ui.channel.list.viewmodel.factory/ChannelListViewModelFactory///PointingToDeclaration/"></a><br/><br/>how to filter the channels<br/><br/>|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel.factory/ChannelListViewModelFactory///PointingToDeclaration/"></a>sort| <a name="io.getstream.chat.android.ui.channel.list.viewmodel.factory/ChannelListViewModelFactory///PointingToDeclaration/"></a><br/><br/>how to sort the channels, defaults to last_updated<br/><br/>|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel.factory/ChannelListViewModelFactory///PointingToDeclaration/"></a>limit| <a name="io.getstream.chat.android.ui.channel.list.viewmodel.factory/ChannelListViewModelFactory///PointingToDeclaration/"></a><br/><br/>how many channels to return<br/><br/>|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel.factory/ChannelListViewModelFactory///PointingToDeclaration/"></a>messageLimit| <a name="io.getstream.chat.android.ui.channel.list.viewmodel.factory/ChannelListViewModelFactory///PointingToDeclaration/"></a><br/><br/>the number of messages to fetch for each channel<br/><br/>|
  


## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel.factory/ChannelListViewModelFactory/ChannelListViewModelFactory/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>[ChannelListViewModelFactory](ChannelListViewModelFactory.md)| <a name="io.getstream.chat.android.ui.channel.list.viewmodel.factory/ChannelListViewModelFactory/ChannelListViewModelFactory/#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a> [androidJvm] @[JvmOverloads](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-overloads/index.html)()  <br/>  <br/>fun [ChannelListViewModelFactory](ChannelListViewModelFactory.md)(filter: FilterObject = Filters.and(<br/>        Filters.eq("type", "messaging"),<br/>        Filters.`in`("members", listOf(ChatDomain.instance().currentUser.id)),<br/>        Filters.or(Filters.notExists("draft"), Filters.ne("draft", true)),<br/>    ), sort: QuerySort&lt;Channel&gt; = ChannelListViewModel.DEFAULT_SORT, limit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 30, messageLimit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 1)how to filter the channels   <br/>|


## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel.factory/ChannelListViewModelFactory/create/#java.lang.Class[TypeParam(bounds=[androidx.lifecycle.ViewModel])]/PointingToDeclaration/"></a>[create](create.md)| <a name="io.getstream.chat.android.ui.channel.list.viewmodel.factory/ChannelListViewModelFactory/create/#java.lang.Class[TypeParam(bounds=[androidx.lifecycle.ViewModel])]/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>open override fun &lt;[T](create.md) : [ViewModel](https://developer.android.com/reference/kotlin/androidx/lifecycle/ViewModel.html)&gt; [create](create.md)(modelClass: [Class](https://developer.android.com/reference/kotlin/java/lang/Class.html)&lt;[T](create.md)&gt;): [T](create.md)  <br/><br/><br/>|

