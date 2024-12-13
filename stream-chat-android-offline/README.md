# Stream Chat Android Offline

This module adds offline support and provides Flow APIs for Stream Chat. Check out the [Offline Support](https://getstream.io/chat/docs/sdk/android/client/guides/offline-support/) guide for more info.

## Setup

To start using this library in your project, see [Dependencies](https://getstream.io/chat/docs/sdk/android/basics/dependencies/), and then [Getting Started](https://getstream.io/chat/docs/sdk/android/client/overview/).

## Offline

Offline support is essential for a good chat user experience, as mobile networks tend to lose connection frequently. This package ensures you can still send messages, reactions and create new channels while offline.

It also implements a retry strategy to resend messages, reactions and channels.

## Easy-to-use observable state

Stream's Chat API exposes dozens of events that all update the chat state. Messages can be created, updated and removed. Channels can be updated, muted, deleted, members can be added to them.

The end result is that you need a lot of boilerplate code to keep your local chat state up to date. This library handles all this logic for you and simply exposes Flow objects to observe the current state.
