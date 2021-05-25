---
title: setDebouncedInputChangedListener
---
//[stream-chat-android-ui-components](../../../index.md)/[io.getstream.chat.android.ui.search](../index.md)/[SearchInputView](index.md)/[setDebouncedInputChangedListener](setDebouncedInputChangedListener.md)



# setDebouncedInputChangedListener  
[androidJvm]  
Content  
fun [setDebouncedInputChangedListener](setDebouncedInputChangedListener.md)(inputChangedListener: [SearchInputView.InputChangedListener](InputChangedListener/index.md)?)  
More info  


Sets a listener for debounced input events. Quick changes to the input will not be passed to this listener, it will only be invoked when the input has been stable for a short while.

  



