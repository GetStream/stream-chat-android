# Stream Chat Livedata & Offline

This repo adds offline support and livedata support to Stream's Chat SDK.

Status: Experimental/Not ready

## Offline

Offline support is essential for a good chat user experience. Mobile networks tend to lose connection frequently.
This package ensures you can still send messages, reactions and create new channels while offline.

It also implements a retry strategy to resend messages, reactions and channels.

## Livedata

Stream's chat API exposes a few dozen events that all update the chat state.
Messages can be created, updated and removed. Channels can be updated, muted, deleted, members can be added.
Reactions are another example.

The end result is that you need a lot of boilerplate code to keep your local chat state up to date.
This library handles all this logic for you and simply exposes Livedata objects that change.

## How it all fits together

Stream's Chat SDKs for Android consist of 3 libraries:

- The low level client (Make API calls and receive events)
- Livedata & offline support (this library)
- The Chat Views and Sample app

# Using this library

Here are the most common ways to use the library

## Create a chat repo

```kotlin
val repo = ChatRepo.Builder(context, client, data.user1).offlineEnabled().userPresenceEnabled().build()
```

## Unread counts

```kotlin
repo.totalUnreadCount.observe
repo.channelUnreadCount.observe
```

## Messages for a channel

This shows how to fetch messages and other common channel level livedata objects

```kotlin
val channelRepo = repo.channel("messaging", "test123")
channelRepo.watch()
channelRepo.messages.observe
channelRepo.getThread("parent-id").observe
channelRepo.reads.observe
channelRepo.typing.observe
channelRepo.loading.observe
```

## Sending a message

Messages are immediately stored in local storage and your livedata objects.
Afterwards they are retried using the retry policy.

```kotlin
channelRepo.sendMessage(Message(user, "hello world"))
```

## Querying channels

```kotlin
val filter = and(eq("type", "messaging"), `in`("members", listOf(user.id)))
val sort : QuerySort? = null
queryRepo = repo.queryChannels(filter, sort)

queryRepo.query(QueryChannelsPaginationRequest(0, 30))
```

## Tips

To prevent race conditions caused by offline being ready before the client is initialized, be sure to replace

```kotlin
Chat.getInstance().getClient().getCurrentUser()
```

With

```kotlin
ChatRepo.instance().getCurrentUser();
```

# Development

* Each user has it's own Room DB. Some of our API responses are user specific. One example is own_reactions on a message. so if you switch users we need to use a different database/storage for the results
* Suspend functions are only used on private methods. Public ones expose livedata objects.
