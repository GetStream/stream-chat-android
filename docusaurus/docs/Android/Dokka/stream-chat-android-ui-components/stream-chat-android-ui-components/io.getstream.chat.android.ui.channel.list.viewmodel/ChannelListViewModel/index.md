---
title: index
sidebar_position: 1
---
//[stream-chat-android-ui-components](../../../index.md)/[io.getstream.chat.android.ui.channel.list.viewmodel](../index.md)/[ChannelListViewModel](index.md)



# ChannelListViewModel  
 [androidJvm] class [ChannelListViewModel](index.md)(**chatDomain**: ChatDomain, **filter**: FilterObject, **sort**: QuerySort&lt;Channel&gt;, **limit**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), **messageLimit**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) : [ViewModel](https://developer.android.com/reference/kotlin/androidx/lifecycle/ViewModel.html)

ViewModel class for [io.getstream.chat.android.ui.channel.list.ChannelListView](../../io.getstream.chat.android.ui.channel.list/ChannelListView/index.md). Responsible for keeping the channels list up to date. Can be bound to the view using [ChannelListViewModel.bindView](../bindView.md) function.

   


## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel///PointingToDeclaration/"></a>chatDomain| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel///PointingToDeclaration/"></a><br/><br/>entry point for all livedata & offline operations<br/><br/>|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel///PointingToDeclaration/"></a>filter| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel///PointingToDeclaration/"></a><br/><br/>filter for querying channels, should never be empty<br/><br/>|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel///PointingToDeclaration/"></a>sort| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel///PointingToDeclaration/"></a><br/><br/>defines the ordering of the channels<br/><br/>|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel///PointingToDeclaration/"></a>limit| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel///PointingToDeclaration/"></a><br/><br/>the maximum number of channels to fetch<br/><br/>|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel///PointingToDeclaration/"></a>messageLimit| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel///PointingToDeclaration/"></a><br/><br/>the number of messages to fetch for each channel<br/><br/>|
  


## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/ChannelListViewModel/#io.getstream.chat.android.livedata.ChatDomain#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a>[ChannelListViewModel](ChannelListViewModel.md)| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/ChannelListViewModel/#io.getstream.chat.android.livedata.ChatDomain#io.getstream.chat.android.client.api.models.FilterObject#io.getstream.chat.android.client.api.models.QuerySort[io.getstream.chat.android.client.models.Channel]#kotlin.Int#kotlin.Int/PointingToDeclaration/"></a> [androidJvm] fun [ChannelListViewModel](ChannelListViewModel.md)(chatDomain: ChatDomain = ChatDomain.instance(), filter: FilterObject = Filters.and(<br/>        Filters.eq("type", "messaging"),<br/>        Filters.`in`("members", listOf(chatDomain.currentUser.id)),<br/>        Filters.or(Filters.notExists("draft"), Filters.ne("draft", true)),<br/>    ), sort: QuerySort&lt;Channel&gt; = DEFAULT_SORT, limit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 30, messageLimit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 1)entry point for all livedata & offline operations   <br/>|


## Types  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel.Action///PointingToDeclaration/"></a>[Action](Action/index.md)| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel.Action///PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>sealed class [Action](Action/index.md)  <br/><br/><br/>|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel.Companion///PointingToDeclaration/"></a>[Companion](Companion/index.md)| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel.Companion///PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>object [Companion](Companion/index.md)  <br/><br/><br/>|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel.PaginationState///PointingToDeclaration/"></a>[PaginationState](PaginationState/index.md)| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel.PaginationState///PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>data class [PaginationState](PaginationState/index.md)(**loadingMore**: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), **endOfChannels**: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html))  <br/><br/><br/>|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel.State///PointingToDeclaration/"></a>[State](State/index.md)| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel.State///PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>data class [State](State/index.md)(**isLoading**: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), **channels**: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;Channel&gt;)  <br/><br/><br/>|


## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/paginationState/#/PointingToDeclaration/"></a>[paginationState](paginationState.md)| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/paginationState/#/PointingToDeclaration/"></a> [androidJvm] val [paginationState](paginationState.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[ChannelListViewModel.PaginationState](PaginationState/index.md)&gt;   <br/>|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/state/#/PointingToDeclaration/"></a>[state](state.md)| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/state/#/PointingToDeclaration/"></a> [androidJvm] val [state](state.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[ChannelListViewModel.State](State/index.md)&gt;   <br/>|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/typingEvents/#/PointingToDeclaration/"></a>[typingEvents](typingEvents.md)| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/typingEvents/#/PointingToDeclaration/"></a> [androidJvm] val [typingEvents](typingEvents.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;TypingEvent&gt;   <br/>|


## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/deleteChannel/#io.getstream.chat.android.client.models.Channel/PointingToDeclaration/"></a>[deleteChannel](deleteChannel.md)| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/deleteChannel/#io.getstream.chat.android.client.models.Channel/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>fun [deleteChannel](deleteChannel.md)(channel: Channel)  <br/><br/><br/>|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/hideChannel/#io.getstream.chat.android.client.models.Channel/PointingToDeclaration/"></a>[hideChannel](hideChannel.md)| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/hideChannel/#io.getstream.chat.android.client.models.Channel/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>fun [hideChannel](hideChannel.md)(channel: Channel)  <br/><br/><br/>|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/leaveChannel/#io.getstream.chat.android.client.models.Channel/PointingToDeclaration/"></a>[leaveChannel](leaveChannel.md)| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/leaveChannel/#io.getstream.chat.android.client.models.Channel/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>fun [leaveChannel](leaveChannel.md)(channel: Channel)  <br/><br/><br/>|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/markAllRead/#/PointingToDeclaration/"></a>[markAllRead](markAllRead.md)| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/markAllRead/#/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>fun [markAllRead](markAllRead.md)()  <br/><br/><br/>|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/onAction/#io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel.Action/PointingToDeclaration/"></a>[onAction](onAction.md)| <a name="io.getstream.chat.android.ui.channel.list.viewmodel/ChannelListViewModel/onAction/#io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel.Action/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>fun [onAction](onAction.md)(action: [ChannelListViewModel.Action](Action/index.md))  <br/><br/><br/>|


## Inherited functions  
  
|  Name |  Summary | 
|---|---|
| <a name="androidx.lifecycle/ViewModel/clear/#/PointingToDeclaration/"></a>[clear](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1936886459%2FFunctions%2F-523872580)| <a name="androidx.lifecycle/ViewModel/clear/#/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>@[MainThread](https://developer.android.com/reference/kotlin/androidx/annotation/MainThread.html)()  <br/>  <br/>fun [clear](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1936886459%2FFunctions%2F-523872580)()  <br/><br/><br/>|
| <a name="androidx.lifecycle/ViewModel/getTag/#kotlin.String/PointingToDeclaration/"></a>[getTag](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-215894976%2FFunctions%2F-523872580)| <a name="androidx.lifecycle/ViewModel/getTag/#kotlin.String/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>open fun &lt;[T](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-215894976%2FFunctions%2F-523872580) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [getTag](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-215894976%2FFunctions%2F-523872580)(p0: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [T](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-215894976%2FFunctions%2F-523872580)  <br/><br/><br/>|
| <a name="androidx.lifecycle/ViewModel/onCleared/#/PointingToDeclaration/"></a>[onCleared](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1930136507%2FFunctions%2F-523872580)| <a name="androidx.lifecycle/ViewModel/onCleared/#/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>open fun [onCleared](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1930136507%2FFunctions%2F-523872580)()  <br/><br/><br/>|
| <a name="androidx.lifecycle/ViewModel/setTagIfAbsent/#kotlin.String#TypeParam(bounds=[kotlin.Any])/PointingToDeclaration/"></a>[setTagIfAbsent](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1567230750%2FFunctions%2F-523872580)| <a name="androidx.lifecycle/ViewModel/setTagIfAbsent/#kotlin.String#TypeParam(bounds=[kotlin.Any])/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>open fun &lt;[T](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1567230750%2FFunctions%2F-523872580) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [setTagIfAbsent](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1567230750%2FFunctions%2F-523872580)(p0: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), p1: [T](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1567230750%2FFunctions%2F-523872580)): [T](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1567230750%2FFunctions%2F-523872580)  <br/><br/><br/>|


## Extensions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.ui.channel.list.viewmodel//bindView/io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel#io.getstream.chat.android.ui.channel.list.ChannelListView#androidx.lifecycle.LifecycleOwner/PointingToDeclaration/"></a>[bindView](../bindView.md)| <a name="io.getstream.chat.android.ui.channel.list.viewmodel//bindView/io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel#io.getstream.chat.android.ui.channel.list.ChannelListView#androidx.lifecycle.LifecycleOwner/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>@[JvmName](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-name/index.html)(name = bind)  <br/>  <br/>fun [ChannelListViewModel](index.md).[bindView](../bindView.md)(view: [ChannelListView](../../io.getstream.chat.android.ui.channel.list/ChannelListView/index.md), lifecycle: [LifecycleOwner](https://developer.android.com/reference/kotlin/androidx/lifecycle/LifecycleOwner.html))  <br/>More info  <br/>Binds [ChannelListView](../../io.getstream.chat.android.ui.channel.list/ChannelListView/index.md) with [ChannelListViewModel](index.md), updating the view's state based on data provided by the ViewModel, and propagating view events to the ViewModel as needed.  <br/><br/><br/>|

