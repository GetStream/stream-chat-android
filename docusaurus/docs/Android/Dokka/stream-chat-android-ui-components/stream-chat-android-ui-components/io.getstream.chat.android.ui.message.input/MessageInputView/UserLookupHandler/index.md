---
title: index
sidebar_position: 1
---
//[stream-chat-android-ui-components](../../../../index.md)/[io.getstream.chat.android.ui.message.input](../../index.md)/[MessageInputView](../index.md)/[UserLookupHandler](index.md)



# UserLookupHandler  
 [androidJvm] interface [UserLookupHandler](index.md)

Users lookup functional interface. Used to create custom users lookup algorithm.

   


## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.ui.message.input/MessageInputView.UserLookupHandler/handleUserLookup/#kotlin.String/PointingToDeclaration/"></a>[handleUserLookup](handleUserLookup.md)| <a name="io.getstream.chat.android.ui.message.input/MessageInputView.UserLookupHandler/handleUserLookup/#kotlin.String/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>abstract suspend fun [handleUserLookup](handleUserLookup.md)(query: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;User&gt;  <br/>More info  <br/>Performs users lookup by given [query](handleUserLookup.md) in suspend way.  <br/><br/><br/>|


## Inheritors  
  
|  Name | 
|---|
| <a name="io.getstream.chat.android.ui.message.input/MessageInputView.DefaultUserLookupHandler///PointingToDeclaration/"></a>[MessageInputView](../DefaultUserLookupHandler/index.md)|

