# Stream Chat Livedata & Offline

This module adds offline support and provides LiveData APIs to [Stream's Chat SDK](https://github.com/GetStream/stream-chat-android). 

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

# Docs

[Docs are here](https://getstream.io/chat/docs/livedata/?language=kotlin)

And API reference (generated using Dokka) is available [here](https://getstream.github.io/stream-chat-android-livedata/library/)


## Tips

To prevent race conditions caused by offline being ready before the client is initialized, be sure to replace

```kotlin
Chat.instance().getClient().getCurrentUser()
```


With

```kotlin
ChatRepo.instance().getCurrentUser()
```

## Dev Tips

To run the test suite create a file called `library/.env` and fill in the following environment variables:

```
STREAM_LOG_LEVEL=ALL
STREAM_API_KEY=yourapikeyhere
STREAM_USER_1_TOKEN=validTokenForUser1
```

## Event Structure Bugs

The low level client and rest API don't define the structure of events. Because of this all fields on the event are nullable which can cause issues.

This JSON file shows the most common events, [event_structure](https://github.com/GetStream/stream-chat-android-livedata/blob/master/livedata/src/test/java/io/getstream/chat/android/livedata/event_structure.json)

The test suite uses the event structure defined in [TestDataHelper](https://github.com/GetStream/stream-chat-android-livedata/blob/master/livedata/src/test/java/io/getstream/chat/android/livedata/utils/TestDataHelper.kt)

So if you run into a crash caused by the event structure the recommended way to fix this is:

1. Lookup the structure of the event in event_structure.json
2. Update the testDataHelper with the actual event structure
3. Fix the error



