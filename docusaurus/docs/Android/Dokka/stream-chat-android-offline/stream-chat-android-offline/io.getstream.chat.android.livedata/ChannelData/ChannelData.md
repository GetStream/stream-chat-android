---
title: ChannelData
---
//[stream-chat-android-offline](../../../index.md)/[io.getstream.chat.android.livedata](../index.md)/[ChannelData](index.md)/[ChannelData](ChannelData.md)



# ChannelData  
[androidJvm]  
Content  
fun [ChannelData](ChannelData.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), channelId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) = "%s:%s".format(type, channelId), createdBy: User = User(), cooldown: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0, frozen: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false, createdAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)? = null, updatedAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)? = null, deletedAt: [Date](https://developer.android.com/reference/kotlin/java/util/Date.html)? = null, memberCount: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0, team: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) = "", extraData: [MutableMap](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; = mutableMapOf())  


[androidJvm]  
Content  
fun [ChannelData](ChannelData.md)(c: Channel)  
More info  


create a ChannelData object from a Channel object

  



