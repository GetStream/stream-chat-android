---
title: errorEvents
---
/[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.offline](../index.md)/[ChatDomain](index.md)/[errorEvents](errorEvents.md)  
  
  
  
# errorEvents  
abstract val [errorEvents](errorEvents.md): StateFlow&lt;[Event](../../io.getstream.chat.android.offline.utils/Event/index.md)&lt;ChatError&gt;&gt;The error event state flow object is triggered when errors in the underlying components occur. The following example shows how to observe these errorsrepo.errorEvent.collect {     // create a toast }
