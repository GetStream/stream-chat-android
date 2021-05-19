---
title: index
sidebar_position: 1
---
/[stream-chat-android-ui-components](../../index.md)/[io.getstream.chat.android.ui.channel.list.header.viewmodel](../index.md)/[ChannelListHeaderViewModel](index.md)  
  
  
  
# ChannelListHeaderViewModel  
class [ChannelListHeaderViewModel](index.md)@[JvmOverloads](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-overloads/index.html)()constructor(**chatDomain**: ChatDomain) : [ViewModel](https://developer.android.com/reference/kotlin/androidx/lifecycle/ViewModel.html)ViewModel class for [io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView](../../io.getstream.chat.android.ui.channel.list.header/ChannelListHeaderView/index.md). Responsible for updating current user information. Can be bound to the view using [ChannelListHeaderViewModel.bindView](../bindView.md) function.  
  
## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.ui.channel.list.header.viewmodel/ChannelListHeaderViewModel///PointingToDeclaration/"></a>chatDomain| <a name="io.getstream.chat.android.ui.channel.list.header.viewmodel/ChannelListHeaderViewModel///PointingToDeclaration/"></a>entry point for offline operations|
  
  
  
## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.ui.channel.list.header.viewmodel/ChannelListHeaderViewModel/ChannelListHeaderViewModel/#io.getstream.chat.android.livedata.ChatDomain/PointingToDeclaration/"></a>[ChannelListHeaderViewModel](ChannelListHeaderViewModel.md)| <a name="io.getstream.chat.android.ui.channel.list.header.viewmodel/ChannelListHeaderViewModel/ChannelListHeaderViewModel/#io.getstream.chat.android.livedata.ChatDomain/PointingToDeclaration/"></a>@[JvmOverloads](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-overloads/index.html)()fun [ChannelListHeaderViewModel](ChannelListHeaderViewModel.md)(chatDomain: ChatDomain = ChatDomain.instance())entry point for offline operations|
  
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.ui.channel.list.header.viewmodel/ChannelListHeaderViewModel/currentUser/#/PointingToDeclaration/"></a>[currentUser](currentUser.md)| <a name="io.getstream.chat.android.ui.channel.list.header.viewmodel/ChannelListHeaderViewModel/currentUser/#/PointingToDeclaration/"></a>val [currentUser](currentUser.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;User&gt;|
| <a name="io.getstream.chat.android.ui.channel.list.header.viewmodel/ChannelListHeaderViewModel/online/#/PointingToDeclaration/"></a>[online](online.md)| <a name="io.getstream.chat.android.ui.channel.list.header.viewmodel/ChannelListHeaderViewModel/online/#/PointingToDeclaration/"></a>val [online](online.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;|
  
  
## Inherited functions  
  
|  Name |  Summary | 
|---|---|
| <a name="androidx.lifecycle/ViewModel/clear/#/PointingToDeclaration/"></a>[clear](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1936886459%2FFunctions%2F-523872580)| <a name="androidx.lifecycle/ViewModel/clear/#/PointingToDeclaration/"></a>@[MainThread](https://developer.android.com/reference/kotlin/androidx/annotation/MainThread.html)()fun [clear](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1936886459%2FFunctions%2F-523872580)()|
| <a name="androidx.lifecycle/ViewModel/getTag/#kotlin.String/PointingToDeclaration/"></a>[getTag](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-215894976%2FFunctions%2F-523872580)| <a name="androidx.lifecycle/ViewModel/getTag/#kotlin.String/PointingToDeclaration/"></a>open fun &lt;[T](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-215894976%2FFunctions%2F-523872580) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [getTag](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-215894976%2FFunctions%2F-523872580)(p0: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [T](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-215894976%2FFunctions%2F-523872580)|
| <a name="androidx.lifecycle/ViewModel/onCleared/#/PointingToDeclaration/"></a>[onCleared](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1930136507%2FFunctions%2F-523872580)| <a name="androidx.lifecycle/ViewModel/onCleared/#/PointingToDeclaration/"></a>open fun [onCleared](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1930136507%2FFunctions%2F-523872580)()|
| <a name="androidx.lifecycle/ViewModel/setTagIfAbsent/#kotlin.String#TypeParam(bounds=[kotlin.Any])/PointingToDeclaration/"></a>[setTagIfAbsent](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1567230750%2FFunctions%2F-523872580)| <a name="androidx.lifecycle/ViewModel/setTagIfAbsent/#kotlin.String#TypeParam(bounds=[kotlin.Any])/PointingToDeclaration/"></a>open fun &lt;[T](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1567230750%2FFunctions%2F-523872580) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [setTagIfAbsent](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1567230750%2FFunctions%2F-523872580)(p0: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), p1: [T](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1567230750%2FFunctions%2F-523872580)): [T](../../io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/index.md#-1567230750%2FFunctions%2F-523872580)|
  
  
## Extensions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.ui.channel.list.header.viewmodel//bindView/io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel#io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView#androidx.lifecycle.LifecycleOwner/PointingToDeclaration/"></a>[bindView](../bindView.md)| <a name="io.getstream.chat.android.ui.channel.list.header.viewmodel//bindView/io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel#io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView#androidx.lifecycle.LifecycleOwner/PointingToDeclaration/"></a>@[JvmName](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-name/index.html)(name = bind)fun [ChannelListHeaderViewModel](index.md).[bindView](../bindView.md)(view: [ChannelListHeaderView](../../io.getstream.chat.android.ui.channel.list.header/ChannelListHeaderView/index.md), lifecycleOwner: [LifecycleOwner](https://developer.android.com/reference/kotlin/androidx/lifecycle/LifecycleOwner.html))Binds [ChannelListHeaderView](../../io.getstream.chat.android.ui.channel.list.header/ChannelListHeaderView/index.md) with [ChannelListHeaderViewModel](index.md), updating the view's state based on data provided by the ViewModel, and propagating view events to the ViewModel as needed.|

