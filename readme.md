# Stream Chat Livedata & Offline

This repo adds offline support and livedata support to Stream's Chat SDK.

## Offline

Offline support is essential for a good chat user experience.
Mobile networks tend to lose connection frequently.
This package ensures you can still send messages, reactions and create new channels while offline.

## Livedata

Stream's chat API exposes a few dozen events that all update the chat state.
Messages can be created, updated and removed. Channels can be updated, muted, deleted, members can be added.
Reactions are another example.

The end result is that you need a lot of logic to keep your local chat state up to date.
This library handles all this logic for you and simply exposes Livedata objects that change.

## How it all fits together

Stream's Chat SDKs for Android have 3 libraries:

- The low level client (Make API calls and receive events)
- Livedata & offline support (this library)
- The Chat Views and Sample app

# Using this library

Here are the most common ways to use the library

## Create a chat repo

```
val repo = ChatRepo(context, user, client, offlineEnabled)
```

## Unread counts

```
repo.totalUnreadCount.observe
repo.channelUnreadCount.observe
```

## Messages for a channel

This shows how to fetch messages and other common channel level livedata objects

```
val channelRepo = repo.channel("messaging", "test123")
channelRepo.watch()
channelRepo.messages.observe
channelRepo.threads.observe
channelRepo.reads.observe
channelRepo.typing.observe
channelRepo.loading.observe
```

## Loading

The loading observable returns an loading data class that specifies the type of loading and what's loading

- READY
- LOADING_FIRST
- LOADING_NEWER
- LOADING_LATER

## Sending a message

Messages are immediately stored in local storage and your livedata objects.
Afterwards they are retried using the retry policy.

```
channelRepo.sendMessage(Message(user, "hello world"))
```

## Querying channels

```
val filter = and(eq("type", "messaging"), `in`("members", listOf(user.id)))
val sort : QuerySort? = null
queryRepo = repo.queryChannels(filter, sort)

// TODO this is a bit ugly
queryRepo.query(QueryChannelsRequest(offset=0, limit=10, messageLimit=100))
```

# Development

* Each user has it's own Room DB. Some of our API responses are user specific. One example is own_reactions on a message. so if you switch users we need to use a different database/storage for the results
* Suspend functions are only used on private methods. Public ones expose livedata objects.
* Room automatically updates the livedata objects it exposes. This library should also work if offline storage is disabled though. So we shouldn't rely on that behaviour from Room.



# Questions/Research:

- User updates are very complex since they are attached to channels, messages, reactions, read state etc. So making sure you update everything efficiently when a user changes their name is hard.
- createChannel, sendMessage & sendReaction need some sort of callback system. Do we use a simple callback or something more like Call<T> like we do in the LLC?
- recover flow depends on design of the backend...