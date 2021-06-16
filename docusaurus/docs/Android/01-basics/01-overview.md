---
slug: /
---

# Overview

The [Stream Chat Android SDK](https://github.com/GetStream/stream-chat-android) enables you to build any type of chat or messaging experience for Android, either in Kotlin or Java. 

This section provides an overview of the SDK, its libraries, and how they fit together.

There are three major components in the SDK:

* [Client](https://github.com/GetStream/stream-chat-android/tree/main/stream-chat-android-client)
* [Offline support](https://github.com/GetStream/stream-chat-android/tree/main/stream-chat-android-offline)
* [UI components](https://github.com/GetStream/stream-chat-android/tree/main/stream-chat-android-ui-components)

### Client

The client library is a low-level wrapper around the Stream Chat API. It lets you authenticate users, handle events, and perform operations such as creating channels and sending messages. Its entry point for all of these capabilities is the `ChatClient` class.

It also contains all the basic model objects you'll interact with when using Stream Chat, such as `User`, `Channel`, or `Message`.

### Offline support

The offline library builds on top of the client and adds offline caching capabilities using a local database. For example, it allows you to send messages or add reactions while you're offline. When the user comes back online, the library will automatically recover lost events and retry sending messages.

This library exposes easy-to-use StateFlow and LiveData objects for reading messages, reads, typing, members, watchers and more. The entry point to this library is the `ChatDomain` class.

If you want to build custom UI for Stream Chat, you can build it on top of the offline library.

### UI components

The UI Components library includes pre-built Android Views to easily load and display data from the Stream Chat API. These include a Channel List and a Message List, a Message Input View, and more. See the UI Components [Getting Started](../03-ui/01-getting-started.md) page for more details.

This library is built on top of the offline support library, and offers the quickest integration of Stream Chat into an Android application.

You can see the components in action by checking out the [UI Components Sample App](https://github.com/GetStream/stream-chat-android/tree/main/stream-chat-android-ui-components-sample), available in the GitHub repository.
