# Overview

The [Stream Chat Jetpack Compose SDK](https://github.com/GetStream/stream-chat-android-compose) helps you build a rich and beautiful chat experience for your users, using the modern Android UI toolkit. Based on our Stream Chat API and the Android low-level Client, it provides a rich set of features you can use to integrate essential Chat app features to your app. It currently supports:

* Rich and customizable messages
* Image and file uploads, downloads and attachment messages
* Custom attachments
* Reactions
* Threads and quoted replies
* Channel and message lists

:::note 
The fastest way to get started with the SDK is by trying the [Jetpack Compose In-App Messaging Tutorial](https://github.com/GetStream/stream/pull/2712/files).
:::

The documentation is split into the following sections:

* **Basics**: An overview of the SDK, the dependencies and installation process and our component architecture overview.
* **Channel Components**: List of our components related to Channels and how to use them.
* **Message Components**: List of our components related to Messages and how to use them.
* **General Customization**: An overview of our core customization options, such as customizing the theme and applying specific theme styles, overriding component behavior and combining multiple components together.
* **Utility Components**: List of our utility components, that you can use with our SDK, or to build your own components.
* **Guides**: Helpful guides that teach you how to apply more UI customization to components, build custom components and compose them together into a complex UI, as well as override component behavior. 

Additionally, there are three major components in the SDK that you can include in your app:

* [Client](https://github.com/GetStream/stream-chat-android/tree/main/stream-chat-android-client)
* [Offline support](https://github.com/GetStream/stream-chat-android/tree/main/stream-chat-android-offline)
* [Jetpack Compose UI components](https://github.com/GetStream/stream-chat-android-compose/tree/main/stream-chat-android-compose)

### Client

The client library is a low-level wrapper around the Stream Chat API. It lets you authenticate users, handle events, and perform operations such as creating channels and sending messages. Its entry point for all of these capabilities is the `ChatClient` class.

It also contains all the basic model objects you'll interact with when using Stream Chat, such as `User`, `Channel`, or `Message`.

### Offline support

The offline library builds on top of the client and adds offline caching capabilities using a local database. For example, it allows you to send messages or add reactions while you're offline. When the user comes back online, the library will automatically recover lost events and retry sending messages.

This library exposes easy-to-use StateFlow and LiveData objects for reading messages, reads, typing, members, watchers and more. The entry point to this library is the `ChatDomain` class.

If you want to build custom UI for Stream Chat, you can build it on top of the offline library.

### Jetpack Compose components

The Jetpack Compose Components library includes three types of composable functions you can use, to easily load and display data from the Stream Chat API:

* **Screen components**: Complete, out-of-the-box screen composables that connect all the operations you need to give users a Chat experience.
* **Bound components**: Components which serve a specific use-case and are bound to a `ViewModel`.
* **Stateless components**: Pure components that rely just on state and expose various events you can handle yourself.

You'll learn more about these components in [Component Architecture](./02-component-architecture.md).

You can see the components in action by checking out the [Jetpack Compose Sample App](https://github.com/GetStream/stream-chat-android-compose/tree/main/app), available in the GitHub repository.
