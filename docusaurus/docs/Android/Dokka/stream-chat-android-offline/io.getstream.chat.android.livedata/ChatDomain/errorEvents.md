---
title: errorEvents
---
/[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.livedata](../index.md)/[ChatDomain](index.md)/[errorEvents](errorEvents.md)  
  
  
  
# errorEvents  
abstract val [errorEvents](errorEvents.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[Event](../../io.getstream.chat.android.livedata.utils/Event/index.md)&lt;ChatError&gt;&gt;The error event livedata object is triggered when errors in the underlying components occure. The following example shows how to observe these errorsrepo.errorEvent.observe(this, EventObserver {     // create a toast })
