---
title: index
sidebar_position: 1
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.livedata.usecase](../index.md)/[SearchUsersByName](index.md)



# SearchUsersByName  
 [androidJvm] class [SearchUsersByName](index.md)

Use case for searching users by string-autocomplete filter. Performs online request if connected or local searching in DB otherwise.

   


## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/SearchUsersByName/invoke/#kotlin.String#kotlin.Int#kotlin.Int#kotlin.Boolean/PointingToDeclaration/"></a>[invoke](invoke.md)| <a name="io.getstream.chat.android.livedata.usecase/SearchUsersByName/invoke/#kotlin.String#kotlin.Int#kotlin.Int#kotlin.Boolean/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>operator fun [invoke](invoke.md)(querySearch: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), offset: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), userLimit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), userPresence: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)): Call&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;User&gt;&gt;  <br/>More info  <br/>Perform api request with a search string as autocomplete if in online state.  <br/><br/><br/>|

