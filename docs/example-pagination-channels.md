# Channels pagination

Get first page

```kotlin
val channel = ChatChannel("id")
val limit = 25
val result = client.queryChannels(channel, ChannelsQuery(limit)).execute()
val channels = result.getData()
```

Get subsequent pages

```kotlin
val channel = ChatChannel("id")
val limit = 25
val offset = 25
val lastMessage = "message-id"
val result = client.queryChannels(channel, ChannelsQuery(limit, offset)).execute()
val channels = result.getData()
```