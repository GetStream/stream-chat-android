---
title: bindView
---
//[stream-chat-android-ui-components](../../index.md)/[io.getstream.chat.android.ui.channel.list.viewmodel](index.md)/[bindView](bindView.md)



# bindView  
[androidJvm]  
Content  
@[JvmName](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-name/index.html)(name = bind)  
  
fun [ChannelListViewModel](ChannelListViewModel/index.md).[bindView](bindView.md)(view: [ChannelListView](../io.getstream.chat.android.ui.channel.list/ChannelListView/index.md), lifecycle: [LifecycleOwner](https://developer.android.com/reference/kotlin/androidx/lifecycle/LifecycleOwner.html))  
More info  


Binds [ChannelListView](../io.getstream.chat.android.ui.channel.list/ChannelListView/index.md) with [ChannelListViewModel](ChannelListViewModel/index.md), updating the view's state based on data provided by the ViewModel, and propagating view events to the ViewModel as needed.



This function sets listeners on the view and ViewModel. Call this method before setting any additional listeners on these objects yourself.

  



