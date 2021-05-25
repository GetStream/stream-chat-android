---
title: index
sidebar_position: 1
---
//[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.offline.utils](index.md)



# Package io.getstream.chat.android.offline.utils  


## Types  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.offline.utils/Event///PointingToDeclaration/"></a>[Event](Event/index.md)| <a name="io.getstream.chat.android.offline.utils/Event///PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>open class [Event](Event/index.md)&lt;out [T](Event/index.md)&gt;(**content**: [T](Event/index.md))  <br/>More info  <br/>Used as a wrapper for data that represents an event.  <br/><br/><br/>|
| <a name="io.getstream.chat.android.offline.utils/RetryPolicy///PointingToDeclaration/"></a>[RetryPolicy](RetryPolicy/index.md)| <a name="io.getstream.chat.android.offline.utils/RetryPolicy///PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>interface [RetryPolicy](RetryPolicy/index.md)  <br/>More info  <br/>When creating a channel, adding a reaction or sending any temporary error will trigger the retry policy The retry policy interface exposes 2 methods<ul><li>shouldRetry: returns a boolean if the request should be retried</li><li>retryTimeout: How many milliseconds to wait till the next attempt</li></ul>  <br/><br/><br/>|

