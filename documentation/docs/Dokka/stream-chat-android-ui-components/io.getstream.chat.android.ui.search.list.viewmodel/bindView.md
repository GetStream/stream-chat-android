---
title: bindView
---
/[stream-chat-android-ui-components](../index.md)/[io.getstream.chat.android.ui.search.list.viewmodel](index.md)/[bindView](bindView.md)  
  
  
  
# bindView  
@[JvmName](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-name/index.html)(name = bind)fun [SearchViewModel](SearchViewModel/index.md).[bindView](bindView.md)(view: [SearchResultListView](../io.getstream.chat.android.ui.search.list/SearchResultListView/index.md), lifecycleOwner: [LifecycleOwner](https://developer.android.com/reference/kotlin/androidx/lifecycle/LifecycleOwner.html))Binds [SearchResultListView](../io.getstream.chat.android.ui.search.list/SearchResultListView/index.md) with [SearchViewModel](SearchViewModel/index.md), updating the view's state based on data provided by the ViewModel, and propagating view events to the ViewModel as needed.This function sets listeners on the view and ViewModel. Call this method before setting any additional listeners on these objects yourself.
