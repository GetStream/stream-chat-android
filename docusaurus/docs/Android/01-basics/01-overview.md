---
slug: /
---

# Overview

The [Stream Chat Android SDK](https://github.com/GetStream/stream-chat-android) enables you to easily build any type of chat or messaging experience for Android, either in Kotlin or Java.

:::note 
The fastest way to get started with the SDK is by trying the [Android In-App Messaging Tutorial](https://getstream.io/tutorials/android-chat/).
:::

This section provides an overview of the SDK, explains the basic concepts, and shows you how to get started with building chat features.

The **UI Components** section describes the ready-to-use Android Views provided by the SDK, and the **Guides** section contains instructions for performing common tasks with the SDK.

There are three major components in the SDK that you can include in your app:

* [Client](https://github.com/GetStream/stream-chat-android/tree/main/stream-chat-android-client)
* [Offline support](https://github.com/GetStream/stream-chat-android/tree/main/stream-chat-android-offline)
* [UI components](https://github.com/GetStream/stream-chat-android/tree/main/stream-chat-android-ui-components)

### Client

The client library is a low-level wrapper around the Stream Chat API. It lets you authenticate users, handle events, and perform operations such as creating channels and sending messages. Its entry point for all of these capabilities is the `ChatClient` class.

It also contains all the basic model objects you'll interact with when using Stream Chat, such as `User`, `Channel`, or `Message`.

### Offline Support

The offline library builds on top of the client and adds offline caching capabilities using a local database. For example, it allows you to send messages or add reactions while you're offline. When the user comes back online, the library will automatically recover lost events and retry sending messages.

This library exposes easy-to-use StateFlow and LiveData objects for reading messages, reads, typing, members, watchers and more. The entry point to this library is the `ChatDomain` class. See [Working with Offline](../02-client/06-guides/06-working-with-offline.md) for more details.

If you want to build custom UI for Stream Chat, you can build it on top of the offline library.

### UI Components

The UI Components library includes pre-built Android Views to easily load and display data from the Stream Chat API. These include a Channel List and a Message List, a Message Input View, and more. See the UI Components [Getting Started](../03-ui/01-getting-started.md) page for more details.

This library is built on top of the offline support library, and offers the quickest integration of Stream Chat into an Android application. It also has a variety of [theming](../03-ui/02-theming.md) options to make it fit your app's needs.

You can see the UI Components in action by checking out the [UI Components Sample App](https://github.com/GetStream/stream-chat-android/tree/main/stream-chat-android-ui-components-sample), available in the GitHub repository.
