---
title: invoke
---
/[stream-chat-android-offline](../../index.md)/[io.getstream.chat.android.livedata.usecase](../index.md)/[SearchUsersByName](index.md)/[invoke](invoke.md)  
  
  
  
# invoke  
operator fun [invoke](invoke.md)(querySearch: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), offset: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), userLimit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), userPresence: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)): Call&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;User&gt;&gt;Perform api request with a search string as autocomplete if in online state. Otherwise performs search by name in local database.  
  
## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.livedata.usecase/SearchUsersByName/invoke/#kotlin.String#kotlin.Int#kotlin.Int#kotlin.Boolean/PointingToDeclaration/"></a>querySearch| <a name="io.getstream.chat.android.livedata.usecase/SearchUsersByName/invoke/#kotlin.String#kotlin.Int#kotlin.Int#kotlin.Boolean/PointingToDeclaration/"></a>Search string used as autocomplete.|
| <a name="io.getstream.chat.android.livedata.usecase/SearchUsersByName/invoke/#kotlin.String#kotlin.Int#kotlin.Int#kotlin.Boolean/PointingToDeclaration/"></a>offset| <a name="io.getstream.chat.android.livedata.usecase/SearchUsersByName/invoke/#kotlin.String#kotlin.Int#kotlin.Int#kotlin.Boolean/PointingToDeclaration/"></a>Offset for paginated requests.|
| <a name="io.getstream.chat.android.livedata.usecase/SearchUsersByName/invoke/#kotlin.String#kotlin.Int#kotlin.Int#kotlin.Boolean/PointingToDeclaration/"></a>userLimit| <a name="io.getstream.chat.android.livedata.usecase/SearchUsersByName/invoke/#kotlin.String#kotlin.Int#kotlin.Int#kotlin.Boolean/PointingToDeclaration/"></a>The page size in the request.|
| <a name="io.getstream.chat.android.livedata.usecase/SearchUsersByName/invoke/#kotlin.String#kotlin.Int#kotlin.Int#kotlin.Boolean/PointingToDeclaration/"></a>userPresence| <a name="io.getstream.chat.android.livedata.usecase/SearchUsersByName/invoke/#kotlin.String#kotlin.Int#kotlin.Int#kotlin.Boolean/PointingToDeclaration/"></a>Presence flag to obtain additional info such as last active date.|
  

