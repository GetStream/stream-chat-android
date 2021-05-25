---
title: markRead
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.livedata](../index.md)/[ChatDomain](index.md)/[markRead](markRead.md)



# markRead  
[androidJvm]  
Content  
abstract fun [markRead](markRead.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): Call&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;  
More info  


Marks all messages of the specified channel as read



#### Return  


executable async Call which completes with [Result](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-result/index.html) having data equal to true if the mark read event was sent or false if there was no need to mark read (i. e. the messages are already marked as read).



## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata/ChatDomain/markRead/#kotlin.String/PointingToDeclaration/"></a>cid| <a name="io.getstream.chat.android.livedata/ChatDomain/markRead/#kotlin.String/PointingToDeclaration/"></a><br/><br/>: the full channel id i. e. messaging:123<br/><br/>|
  
  



