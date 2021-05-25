---
title: index
sidebar_position: 1
---
//[stream-chat-android-ui-components](../../../index.md)/[io.getstream.chat.android.ui.mention.list.viewmodel](../index.md)/[MentionListViewModel](index.md)



# MentionListViewModel  
 [androidJvm] class [MentionListViewModel](index.md) : [ViewModel](https://developer.android.com/reference/kotlin/androidx/lifecycle/ViewModel.html)   


## Types  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.ui.mention.list.viewmodel/MentionListViewModel.State///PointingToDeclaration/"></a>[State](State/index.md)| <a name="io.getstream.chat.android.ui.mention.list.viewmodel/MentionListViewModel.State///PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>data class [State](State/index.md)(**canLoadMore**: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), **results**: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;Message&gt;, **isLoading**: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html))  <br/><br/><br/>|


## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.ui.mention.list.viewmodel/MentionListViewModel/errorEvents/#/PointingToDeclaration/"></a>[errorEvents](errorEvents.md)| <a name="io.getstream.chat.android.ui.mention.list.viewmodel/MentionListViewModel/errorEvents/#/PointingToDeclaration/"></a> [androidJvm] val [errorEvents](errorEvents.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;Event&lt;[Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)&gt;&gt;   <br/>|
| <a name="io.getstream.chat.android.ui.mention.list.viewmodel/MentionListViewModel/state/#/PointingToDeclaration/"></a>[state](state.md)| <a name="io.getstream.chat.android.ui.mention.list.viewmodel/MentionListViewModel/state/#/PointingToDeclaration/"></a> [androidJvm] val [state](state.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[MentionListViewModel.State](State/index.md)&gt;   <br/>|


## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.ui.mention.list.viewmodel/MentionListViewModel/loadMore/#/PointingToDeclaration/"></a>[loadMore](loadMore.md)| <a name="io.getstream.chat.android.ui.mention.list.viewmodel/MentionListViewModel/loadMore/#/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>fun [loadMore](loadMore.md)()  <br/><br/><br/>|


## Inherited functions  
  
|  Name |  Summary | 
|---|---|
| <a name="androidx.lifecycle/ViewModel/clear/#/PointingToDeclaration/"></a>[clear](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1936886459%2FFunctions%2F-523872580)| <a name="androidx.lifecycle/ViewModel/clear/#/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>@[MainThread](https://developer.android.com/reference/kotlin/androidx/annotation/MainThread.html)()  <br/>  <br/>fun [clear](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1936886459%2FFunctions%2F-523872580)()  <br/><br/><br/>|
| <a name="androidx.lifecycle/ViewModel/getTag/#kotlin.String/PointingToDeclaration/"></a>[getTag](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-215894976%2FFunctions%2F-523872580)| <a name="androidx.lifecycle/ViewModel/getTag/#kotlin.String/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>open fun &lt;[T](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-215894976%2FFunctions%2F-523872580) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [getTag](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-215894976%2FFunctions%2F-523872580)(p0: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [T](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-215894976%2FFunctions%2F-523872580)  <br/><br/><br/>|
| <a name="androidx.lifecycle/ViewModel/setTagIfAbsent/#kotlin.String#TypeParam(bounds=[kotlin.Any])/PointingToDeclaration/"></a>[setTagIfAbsent](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1567230750%2FFunctions%2F-523872580)| <a name="androidx.lifecycle/ViewModel/setTagIfAbsent/#kotlin.String#TypeParam(bounds=[kotlin.Any])/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>open fun &lt;[T](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1567230750%2FFunctions%2F-523872580) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [setTagIfAbsent](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1567230750%2FFunctions%2F-523872580)(p0: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), p1: [T](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1567230750%2FFunctions%2F-523872580)): [T](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1567230750%2FFunctions%2F-523872580)  <br/><br/><br/>|


## Extensions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.ui.mention.list.viewmodel//bindView/io.getstream.chat.android.ui.mention.list.viewmodel.MentionListViewModel#io.getstream.chat.android.ui.mention.list.MentionListView#androidx.lifecycle.LifecycleOwner/PointingToDeclaration/"></a>[bindView](../bindView.md)| <a name="io.getstream.chat.android.ui.mention.list.viewmodel//bindView/io.getstream.chat.android.ui.mention.list.viewmodel.MentionListViewModel#io.getstream.chat.android.ui.mention.list.MentionListView#androidx.lifecycle.LifecycleOwner/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>@[JvmName](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-name/index.html)(name = bind)  <br/>  <br/>fun [MentionListViewModel](index.md).[bindView](../bindView.md)(view: [MentionListView](../../io.getstream.chat.android.ui.mention.list/MentionListView/index.md), lifecycleOwner: [LifecycleOwner](https://developer.android.com/reference/kotlin/androidx/lifecycle/LifecycleOwner.html))  <br/>More info  <br/>Binds [MentionListView](../../io.getstream.chat.android.ui.mention.list/MentionListView/index.md) with [MentionListViewModel](index.md), updating the view's state based on data provided by the ViewModel and propagating view events to the ViewModel as needed.  <br/><br/><br/>|

