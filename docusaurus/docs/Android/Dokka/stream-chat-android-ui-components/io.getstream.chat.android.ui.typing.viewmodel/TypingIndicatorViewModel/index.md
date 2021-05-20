---
title: index
sidebar_position: 1
---
/[stream-chat-android-ui-components](../../index.md)/[io.getstream.chat.android.ui.typing.viewmodel](../index.md)/[TypingIndicatorViewModel](index.md)  
  
  
  
# TypingIndicatorViewModel  
class [TypingIndicatorViewModel](index.md)(**cid**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **chatDomain**: ChatDomain) : [ViewModel](https://developer.android.com/reference/kotlin/androidx/lifecycle/ViewModel.html)  
  
## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/typingUsers/#/PointingToDeclaration/"></a>[typingUsers](typingUsers.md)| <a name="io.getstream.chat.android.ui.typing.viewmodel/TypingIndicatorViewModel/typingUsers/#/PointingToDeclaration/"></a>val [typingUsers](typingUsers.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;User&gt;&gt;|
  
  
## Inherited functions  
  
|  Name |  Summary | 
|---|---|
| <a name="androidx.lifecycle/ViewModel/clear/#/PointingToDeclaration/"></a>[clear](index.md#-1936886459%2FFunctions%2F-523872580)| <a name="androidx.lifecycle/ViewModel/clear/#/PointingToDeclaration/"></a>@[MainThread](https://developer.android.com/reference/kotlin/androidx/annotation/MainThread.html)()fun [clear](index.md#-1936886459%2FFunctions%2F-523872580)()|
| <a name="androidx.lifecycle/ViewModel/getTag/#kotlin.String/PointingToDeclaration/"></a>[getTag](index.md#-215894976%2FFunctions%2F-523872580)| <a name="androidx.lifecycle/ViewModel/getTag/#kotlin.String/PointingToDeclaration/"></a>open fun &lt;[T](index.md#-215894976%2FFunctions%2F-523872580) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [getTag](index.md#-215894976%2FFunctions%2F-523872580)(p0: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [T](index.md#-215894976%2FFunctions%2F-523872580)|
| <a name="androidx.lifecycle/ViewModel/onCleared/#/PointingToDeclaration/"></a>[onCleared](index.md#-1930136507%2FFunctions%2F-523872580)| <a name="androidx.lifecycle/ViewModel/onCleared/#/PointingToDeclaration/"></a>open fun [onCleared](index.md#-1930136507%2FFunctions%2F-523872580)()|
| <a name="androidx.lifecycle/ViewModel/setTagIfAbsent/#kotlin.String#TypeParam(bounds=[kotlin.Any])/PointingToDeclaration/"></a>[setTagIfAbsent](index.md#-1567230750%2FFunctions%2F-523872580)| <a name="androidx.lifecycle/ViewModel/setTagIfAbsent/#kotlin.String#TypeParam(bounds=[kotlin.Any])/PointingToDeclaration/"></a>open fun &lt;[T](index.md#-1567230750%2FFunctions%2F-523872580) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [setTagIfAbsent](index.md#-1567230750%2FFunctions%2F-523872580)(p0: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), p1: [T](index.md#-1567230750%2FFunctions%2F-523872580)): [T](index.md#-1567230750%2FFunctions%2F-523872580)|
  
  
## Extensions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.ui.typing.viewmodel//bindView/io.getstream.chat.android.ui.typing.viewmodel.TypingIndicatorViewModel#io.getstream.chat.android.ui.typing.TypingIndicatorView#androidx.lifecycle.LifecycleOwner/PointingToDeclaration/"></a>[bindView](../bindView.md)| <a name="io.getstream.chat.android.ui.typing.viewmodel//bindView/io.getstream.chat.android.ui.typing.viewmodel.TypingIndicatorViewModel#io.getstream.chat.android.ui.typing.TypingIndicatorView#androidx.lifecycle.LifecycleOwner/PointingToDeclaration/"></a>@[JvmName](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-name/index.html)(name = bind)fun [TypingIndicatorViewModel](index.md).[bindView](../bindView.md)(view: [TypingIndicatorView](../../io.getstream.chat.android.ui.typing/TypingIndicatorView/index.md), lifecycleOwner: [LifecycleOwner](https://developer.android.com/reference/kotlin/androidx/lifecycle/LifecycleOwner.html))Binds [TypingIndicatorView](../../io.getstream.chat.android.ui.typing/TypingIndicatorView/index.md) with [TypingIndicatorViewModel](index.md), updating the view's state based on data provided by the ViewModel.|

