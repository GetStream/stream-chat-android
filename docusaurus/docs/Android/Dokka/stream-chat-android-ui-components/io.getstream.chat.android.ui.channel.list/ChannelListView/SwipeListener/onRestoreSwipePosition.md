---
title: onRestoreSwipePosition
---
/[stream-chat-android-ui-components](../../../index.md)/[io.getstream.chat.android.ui.channel.list](../../index.md)/[ChannelListView](../index.md)/[SwipeListener](index.md)/[onRestoreSwipePosition](onRestoreSwipePosition.md)  
  
  
  
# onRestoreSwipePosition  
abstract fun [onRestoreSwipePosition](onRestoreSwipePosition.md)(viewHolder: [SwipeViewHolder](../../../io.getstream.chat.android.ui.channel.list.adapter.viewholder/SwipeViewHolder/index.md), adapterPosition: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html))Invoked in order to set the [viewHolder](onRestoreSwipePosition.md)'s initial state when bound. This supports view holder reuse. When items are scrolled off-screen and the view holder is reused, it becomes important to track the swiped state and determine if the view holder should appear as swiped for the item being bound.  
  
## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.ui.channel.list/ChannelListView.SwipeListener/onRestoreSwipePosition/#io.getstream.chat.android.ui.channel.list.adapter.viewholder.SwipeViewHolder#kotlin.Int/PointingToDeclaration/"></a>viewHolder| <a name="io.getstream.chat.android.ui.channel.list/ChannelListView.SwipeListener/onRestoreSwipePosition/#io.getstream.chat.android.ui.channel.list.adapter.viewholder.SwipeViewHolder#kotlin.Int/PointingToDeclaration/"></a>the view holder being bound|
| <a name="io.getstream.chat.android.ui.channel.list/ChannelListView.SwipeListener/onRestoreSwipePosition/#io.getstream.chat.android.ui.channel.list.adapter.viewholder.SwipeViewHolder#kotlin.Int/PointingToDeclaration/"></a>adapterPosition| <a name="io.getstream.chat.android.ui.channel.list/ChannelListView.SwipeListener/onRestoreSwipePosition/#io.getstream.chat.android.ui.channel.list.adapter.viewholder.SwipeViewHolder#kotlin.Int/PointingToDeclaration/"></a>the internal adapter position of the item being bound|
  

