---
title: bindView
---
/[stream-chat-android-ui-components](../index.md)/[io.getstream.chat.android.ui.typing.viewmodel](index.md)/[bindView](bindView.md)  
  
  
  
# bindView  
@[JvmName](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-name/index.html)(name = bind)fun [TypingIndicatorViewModel](TypingIndicatorViewModel/index.md).[bindView](bindView.md)(view: [TypingIndicatorView](../io.getstream.chat.android.ui.typing/TypingIndicatorView/index.md), lifecycleOwner: [LifecycleOwner](https://developer.android.com/reference/kotlin/androidx/lifecycle/LifecycleOwner.html))Binds [TypingIndicatorView](../io.getstream.chat.android.ui.typing/TypingIndicatorView/index.md) with [TypingIndicatorViewModel](TypingIndicatorViewModel/index.md), updating the view's state based on data provided by the ViewModel.This function sets listeners on the view and ViewModel. Call this method before setting any additional listeners on these objects yourself.
