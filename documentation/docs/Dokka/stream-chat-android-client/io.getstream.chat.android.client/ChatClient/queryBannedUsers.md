---
title: queryBannedUsers
---
/[stream-chat-android-client](../../index.md)/[io.getstream.chat.android.client](../index.md)/[ChatClient](index.md)/[queryBannedUsers](queryBannedUsers.md)  
  
  
  
# queryBannedUsers  
@[CheckResult](https://developer.android.com/reference/kotlin/androidx/annotation/CheckResult.html)()@[JvmOverloads](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-overloads/index.html)()fun [queryBannedUsers](queryBannedUsers.md)(filter: [FilterObject](../../io.getstream.chat.android.client.api.models/FilterObject/index.md), sort: [QuerySort](../../io.getstream.chat.android.client.api.models/QuerySort/index.md)&lt;[BannedUsersSort](../../io.getstream.chat.android.client.models/BannedUsersSort/index.md)&gt; = QuerySort.asc(BannedUsersSort::createdAt), offset: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)? = null, limit: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)? = null, createdAtAfter: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)? = null, createdAtAfterOrEqual: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)? = null, createdAtBefore: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)? = null, createdAtBeforeOrEqual: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)? = null): Call&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[BannedUser](../../io.getstream.chat.android.client.models/BannedUser/index.md)&gt;&gt;
