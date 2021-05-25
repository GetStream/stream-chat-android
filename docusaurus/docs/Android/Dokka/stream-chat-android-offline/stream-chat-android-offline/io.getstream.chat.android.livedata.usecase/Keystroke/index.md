---
title: index
sidebar_position: 1
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.livedata.usecase](../index.md)/[Keystroke](index.md)



# Keystroke  
 [androidJvm] interface [Keystroke](index.md)   


## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/Keystroke/invoke/#kotlin.String#kotlin.String?/PointingToDeclaration/"></a>[invoke](invoke.md)| <a name="io.getstream.chat.android.livedata.usecase/Keystroke/invoke/#kotlin.String#kotlin.String?/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>@[CheckResult](https://developer.android.com/reference/kotlin/androidx/annotation/CheckResult.html)()  <br/>  <br/>abstract operator fun [invoke](invoke.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), parentId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null): Call&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;  <br/>More info  <br/>Keystroke should be called whenever a user enters text into the message input It automatically calls stopTyping when the user stops typing after 5 seconds  <br/><br/><br/>|

