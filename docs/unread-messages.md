# Unread messages
`Channel` object has `read` field which contains unread data per user. Extension function `getUnreadMessagesCount` can be used to get total unread messages.
```kotlin
val channelType = "messaging"
val channelId = "channel-id"
val userId = "user-id"
val request = QueryChannelRequest().withState()

client.queryChannel(channelType, channelId, request).enqueue { result ->

    if (result.isSuccess) {
        val channel = result.data()
        val totalUnreadMessages = channel.getUnreadMessagesCount()
        val unreadMessagesOfAUser = channel.read.first {
            it.user.id == userId
        }.unreadMessages
    } else {
        val error = result.error()
        error.printStackTrace()
        println(error.message)
    }

}
```