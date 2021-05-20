---
title: bindView
---
/[stream-chat-android-ui-components](../index.md)/[io.getstream.chat.android.ui.message.input.viewmodel](index.md)/[bindView](bindView.md)  
  
  
  
# bindView  
@[JvmName](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-name/index.html)(name = bind)fun MessageInputViewModel.[bindView](bindView.md)(view: [MessageInputView](../io.getstream.chat.android.ui.message.input/MessageInputView/index.md), lifecycleOwner: [LifecycleOwner](https://developer.android.com/reference/kotlin/androidx/lifecycle/LifecycleOwner.html))Binds [MessageInputView](../io.getstream.chat.android.ui.message.input/MessageInputView/index.md) with MessageInputViewModel, updating the view's state based on data provided by the ViewModel, and forwarding View events to the ViewModel.This function sets listeners on the view and ViewModel. Call this method before setting any additional listeners on these objects yourself.
