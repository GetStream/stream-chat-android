---
title: index
sidebar_position: 1
---
/[stream-chat-android-ui-components](../../index.md)/[io.getstream.chat.android.ui.search.list.viewmodel](../index.md)/[SearchViewModel](index.md)  
  
  
  
# SearchViewModel  
class [SearchViewModel](index.md) : [ViewModel](https://developer.android.com/reference/kotlin/androidx/lifecycle/ViewModel.html)  
  
## Types  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.ui.search.list.viewmodel/SearchViewModel.State///PointingToDeclaration/"></a>[State](State/index.md)| <a name="io.getstream.chat.android.ui.search.list.viewmodel/SearchViewModel.State///PointingToDeclaration/"></a>data class [State](State/index.md)(**query**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **canLoadMore**: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), **results**: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;Message&gt;, **isLoading**: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html))|
  
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.ui.search.list.viewmodel/SearchViewModel/errorEvents/#/PointingToDeclaration/"></a>[errorEvents](errorEvents.md)| <a name="io.getstream.chat.android.ui.search.list.viewmodel/SearchViewModel/errorEvents/#/PointingToDeclaration/"></a>val [errorEvents](errorEvents.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;Event&lt;[Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)&gt;&gt;|
| <a name="io.getstream.chat.android.ui.search.list.viewmodel/SearchViewModel/state/#/PointingToDeclaration/"></a>[state](state.md)| <a name="io.getstream.chat.android.ui.search.list.viewmodel/SearchViewModel/state/#/PointingToDeclaration/"></a>val [state](state.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[SearchViewModel.State](State/index.md)&gt;|
  
  
## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.ui.search.list.viewmodel/SearchViewModel/loadMore/#/PointingToDeclaration/"></a>[loadMore](loadMore.md)| <a name="io.getstream.chat.android.ui.search.list.viewmodel/SearchViewModel/loadMore/#/PointingToDeclaration/"></a>fun [loadMore](loadMore.md)()|
| <a name="io.getstream.chat.android.ui.search.list.viewmodel/SearchViewModel/setQuery/#kotlin.String/PointingToDeclaration/"></a>[setQuery](setQuery.md)| <a name="io.getstream.chat.android.ui.search.list.viewmodel/SearchViewModel/setQuery/#kotlin.String/PointingToDeclaration/"></a>fun [setQuery](setQuery.md)(query: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))|
  
  
## Inherited functions  
  
|  Name |  Summary | 
|---|---|
| <a name="androidx.lifecycle/ViewModel/clear/#/PointingToDeclaration/"></a>[clear](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1936886459%2FFunctions%2F-523872580)| <a name="androidx.lifecycle/ViewModel/clear/#/PointingToDeclaration/"></a>@[MainThread](https://developer.android.com/reference/kotlin/androidx/annotation/MainThread.html)()fun [clear](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1936886459%2FFunctions%2F-523872580)()|
| <a name="androidx.lifecycle/ViewModel/getTag/#kotlin.String/PointingToDeclaration/"></a>[getTag](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-215894976%2FFunctions%2F-523872580)| <a name="androidx.lifecycle/ViewModel/getTag/#kotlin.String/PointingToDeclaration/"></a>open fun &lt;[T](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-215894976%2FFunctions%2F-523872580) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [getTag](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-215894976%2FFunctions%2F-523872580)(p0: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [T](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-215894976%2FFunctions%2F-523872580)|
| <a name="androidx.lifecycle/ViewModel/setTagIfAbsent/#kotlin.String#TypeParam(bounds=[kotlin.Any])/PointingToDeclaration/"></a>[setTagIfAbsent](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1567230750%2FFunctions%2F-523872580)| <a name="androidx.lifecycle/ViewModel/setTagIfAbsent/#kotlin.String#TypeParam(bounds=[kotlin.Any])/PointingToDeclaration/"></a>open fun &lt;[T](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1567230750%2FFunctions%2F-523872580) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [setTagIfAbsent](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1567230750%2FFunctions%2F-523872580)(p0: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), p1: [T](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1567230750%2FFunctions%2F-523872580)): [T](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1567230750%2FFunctions%2F-523872580)|
  
  
## Extensions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.ui.search.list.viewmodel//bindView/io.getstream.chat.android.ui.search.list.viewmodel.SearchViewModel#io.getstream.chat.android.ui.search.list.SearchResultListView#androidx.lifecycle.LifecycleOwner/PointingToDeclaration/"></a>[bindView](../bindView.md)| <a name="io.getstream.chat.android.ui.search.list.viewmodel//bindView/io.getstream.chat.android.ui.search.list.viewmodel.SearchViewModel#io.getstream.chat.android.ui.search.list.SearchResultListView#androidx.lifecycle.LifecycleOwner/PointingToDeclaration/"></a>@[JvmName](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-name/index.html)(name = bind)fun [SearchViewModel](index.md).[bindView](../bindView.md)(view: [SearchResultListView](../../io.getstream.chat.android.ui.search.list/SearchResultListView/index.md), lifecycleOwner: [LifecycleOwner](https://developer.android.com/reference/kotlin/androidx/lifecycle/LifecycleOwner.html))Binds [SearchResultListView](../../io.getstream.chat.android.ui.search.list/SearchResultListView/index.md) with [SearchViewModel](index.md), updating the view's state based on data provided by the ViewModel.|

