---
title: onSwipeCanceled
---
//[stream-chat-android-ui-components](../../../../index.md)/[io.getstream.chat.android.ui.channel.list](../../index.md)/[ChannelListView](../index.md)/[SwipeListener](index.md)/[onSwipeCanceled](onSwipeCanceled.md)



# onSwipeCanceled  
[androidJvm]  
Content  
abstract fun [onSwipeCanceled](onSwipeCanceled.md)(viewHolder: [SwipeViewHolder](../../../io.getstream.chat.android.ui.channel.list.adapter.viewholder/SwipeViewHolder/index.md), adapterPosition: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), x: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)? = null, y: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)? = null)  
More info  


Invoked when a swipe is canceled.



## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.ui.channel.list/ChannelListView.SwipeListener/onSwipeCanceled/#io.getstream.chat.android.ui.channel.list.adapter.viewholder.SwipeViewHolder#kotlin.Int#kotlin.Float?#kotlin.Float?/PointingToDeclaration/"></a>viewHolder| <a name="io.getstream.chat.android.ui.channel.list/ChannelListView.SwipeListener/onSwipeCanceled/#io.getstream.chat.android.ui.channel.list.adapter.viewholder.SwipeViewHolder#kotlin.Int#kotlin.Float?#kotlin.Float?/PointingToDeclaration/"></a><br/><br/>the view holder that is being swiped<br/><br/>|
| <a name="io.getstream.chat.android.ui.channel.list/ChannelListView.SwipeListener/onSwipeCanceled/#io.getstream.chat.android.ui.channel.list.adapter.viewholder.SwipeViewHolder#kotlin.Int#kotlin.Float?#kotlin.Float?/PointingToDeclaration/"></a>adapterPosition| <a name="io.getstream.chat.android.ui.channel.list/ChannelListView.SwipeListener/onSwipeCanceled/#io.getstream.chat.android.ui.channel.list.adapter.viewholder.SwipeViewHolder#kotlin.Int#kotlin.Float?#kotlin.Float?/PointingToDeclaration/"></a><br/><br/>the internal adapter position of the item being bound<br/><br/>|
| <a name="io.getstream.chat.android.ui.channel.list/ChannelListView.SwipeListener/onSwipeCanceled/#io.getstream.chat.android.ui.channel.list.adapter.viewholder.SwipeViewHolder#kotlin.Int#kotlin.Float?#kotlin.Float?/PointingToDeclaration/"></a>x| <a name="io.getstream.chat.android.ui.channel.list/ChannelListView.SwipeListener/onSwipeCanceled/#io.getstream.chat.android.ui.channel.list.adapter.viewholder.SwipeViewHolder#kotlin.Int#kotlin.Float?#kotlin.Float?/PointingToDeclaration/"></a><br/><br/>the raw X of the swipe origin; null may indicate the call isn't from user interaction<br/><br/>|
| <a name="io.getstream.chat.android.ui.channel.list/ChannelListView.SwipeListener/onSwipeCanceled/#io.getstream.chat.android.ui.channel.list.adapter.viewholder.SwipeViewHolder#kotlin.Int#kotlin.Float?#kotlin.Float?/PointingToDeclaration/"></a>y| <a name="io.getstream.chat.android.ui.channel.list/ChannelListView.SwipeListener/onSwipeCanceled/#io.getstream.chat.android.ui.channel.list.adapter.viewholder.SwipeViewHolder#kotlin.Int#kotlin.Float?#kotlin.Float?/PointingToDeclaration/"></a><br/><br/>the raw Y of the swipe origin; null may indicate the call isn't from user interaction<br/><br/>|
  
  



