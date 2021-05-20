---
title: sendGiphy
---
/[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.livedata.usecase](../index.md)/[UseCaseHelper](index.md)/[sendGiphy](sendGiphy.md)  
  
  
  
# sendGiphy  
val [sendGiphy](sendGiphy.md): [SendGiphy](../SendGiphy/index.md)Sends selected giphy message to the channel. Replaces the original "ephemeral" message in local storage with the one received from backend. Returns new "ephemeral" message with new giphy url. API call to remove the message is retried according to the retry policy specified on the chatDomain
