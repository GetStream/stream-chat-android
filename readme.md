![latest-version](https://jitpack.io/v/GetStream/stream-chat-android-livedata.svg)
[![Build Status](https://travis-ci.com/GetStream/stream-chat-android-livedata.svg?branch=master)](https://travis-ci.com/GetStream/stream-chat-android-livedata)
[![codecov](https://codecov.io/gh/GetStream/stream-chat-android-livedata/branch/master/graph/badge.svg)](https://codecov.io/gh/GetStream/stream-chat-android-livedata)


# Stream Chat Livedata & Offline

This repo adds offline support and livedata support to Stream's Chat SDK.

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
Chat.getInstance().getClient().getCurrentUser()
```


With

```kotlin
ChatRepo.instance().getCurrentUser();
```

## Dev Tips

Run the jacoco task and have a look at coverage here:
```
open library/build/reports/jacoco/jacocoTestDebugUnitTestReport/html/index.html
```
