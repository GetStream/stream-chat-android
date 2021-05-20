---
title: bindView
---
/[stream-chat-android-ui-components](../index.md)/[io.getstream.chat.android.ui.message.list.viewmodel](index.md)/[bindView](bindView.md)  
  
  
  
# bindView  
@[JvmName](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-name/index.html)(name = bind)fun MessageListViewModel.[bindView](bindView.md)(view: [MessageListView](../io.getstream.chat.android.ui.message.list/MessageListView/index.md), lifecycleOwner: [LifecycleOwner](https://developer.android.com/reference/kotlin/androidx/lifecycle/LifecycleOwner.html))Binds [MessageListView](../io.getstream.chat.android.ui.message.list/MessageListView/index.md) with MessageListViewModel, updating the view's state based on data provided by the ViewModel, and forwarding View events to the ViewModel.This function sets listeners on the view and ViewModel. Call this method before setting any additional listeners on these objects yourself.
