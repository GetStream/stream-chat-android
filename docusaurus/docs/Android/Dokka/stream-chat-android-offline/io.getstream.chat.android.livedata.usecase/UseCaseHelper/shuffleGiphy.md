---
title: shuffleGiphy
---
/[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.livedata.usecase](../index.md)/[UseCaseHelper](index.md)/[shuffleGiphy](shuffleGiphy.md)  
  
  
  
# shuffleGiphy  
val [shuffleGiphy](shuffleGiphy.md): [ShuffleGiphy](../ShuffleGiphy/index.md)Performs giphy shuffle operation. Removes the original "ephemeral" message from local storage. Returns new "ephemeral" message with new giphy url. API call to remove the message is retried according to the retry policy specified on the chatDomain
