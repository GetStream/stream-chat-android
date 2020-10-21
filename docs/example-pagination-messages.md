# Messages pagination

Get first page

```kotlin
val channel = ChatChannel("id")
val limit = 25
val result = client.queryChannel(channel, ChannelQuery().withMessages(limit)).execute()
val messages = result.getData().getMessages()
```

Get subsequent pages

```kotlin
val channel = ChatChannel("id")
val limit = 25
val lastMessage = "message-id"
val result = client.queryChannel(channel, ChannelQuery().withMessages(lastMessage, limit)).execute()
val messages = result.getData().getMessages()
```