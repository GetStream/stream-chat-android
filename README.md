# Official Android SDK for [Stream Chat](https://getstream.io/chat/sdk/android/)

<p align="center">
  <a href="https://getstream.io/tutorials/android-chat/">
    <img src="/docs/sdk-hero-android.png"/>
  </a>
</p>

<p align="center">
  <a href="https://github.com/GetStream/stream-chat-android/actions"><img src="https://github.com/GetStream/stream-chat-android/workflows/Build%20and%20test/badge.svg" /></a>
  <a href="https://github.com/GetStream/stream-chat-android/releases"><img src="https://img.shields.io/github/v/release/GetStream/stream-chat-android" /></a>
</p>

This is the official Android SDK for [Stream Chat](https://getstream.io/chat/sdk/android/), a service for building chat and messaging applications. This library includes both a low-level chat SDK and a set of reusable UI components. Most users start with the UI components, and fall back to the lower level API when they want to customize things.

The SDK supports both Kotlin and Java, but *we strongly recommend using Kotlin*.

### 🔗 Quick Links

* [Register](https://getstream.io/chat/trial/): Create an account and get an API key for Stream Chat
* [Chat Tutorial](https://getstream.io/tutorials/android-chat/#kotlin): Learn the basics of the SDK by by building a simple messaging app (Kotlin or Java)
* [UI Components sample app](/stream-chat-android-ui-components-sample): Full messaging app with threads, reactions, optimistic UI updates and offline storage
* [Compose UI Components sample app](/stream-chat-android-compose-sample): Messaging sample app built with Jetpack Compose!
* [Client Documentation](https://getstream.io/chat/docs/android/?language=kotlin)
* [UI Components Documentation](https://getstream.io/chat/docs/sdk/android/)
* [Compose UI Components Documentation](https://getstream.io/chat/docs/sdk/android/compose/overview/)
* [API docs](https://getstream.github.io/stream-chat-android/): Full generated docs from Dokka 

## 👩‍💻 Free for Makers 👨‍💻

Stream is free for most side and hobby projects. To qualify, your project/company needs to have < 5 team members and < $10k in monthly revenue.
For complete pricing details, visit our [Chat Pricing Page](https://getstream.io/chat/pricing/).

## 🗺️ Overview and Documentation 📚

This SDK consists of three main artifacts you can build on:

- [**Client**](/stream-chat-android-client): A low-level client for making API calls and receiving chat events.
    - [Documentation website](https://getstream.io/chat/docs/android/?language=kotlin)
- [**Offline support**](/stream-chat-android-offline): Local caching and automatic retries, exposed via Flow and LiveData APIs.
    - [Working with Offline Support](https://getstream.io/chat/docs/sdk/android/client/guides/working-with-offline/)
- [**UI Components**](/stream-chat-android-ui-components): Reusable and customizable chat Views for displaying conversations, lists of channels, and more!
    - [Sample app](/stream-chat-android-ui-components-sample)
    - [Documentation](https://getstream.io/chat/docs/sdk/android/ui/overview/)

We also support Jetpack Compose! You can use our Compose UI Components as an alternative to the **UI Components**.

- [**Compose UI Components**](/stream-chat-android-compose): Reusable and modular Composables for displaying conversations, lists of channels, and more!
  - [Sample app](/stream-chat-android-compose-sample)
  - [Documentation](https://getstream.io/chat/docs/sdk/android/compose/overview/)

Learn more about the modules by visiting [the documentation](https://getstream.io/chat/docs/sdk/android/).

## 📖 Tutorial

The best place to start is the [Android In-App Messaging Tutorial](https://getstream.io/tutorials/android-chat/). It teaches you the basics of using this SDK and also shows how to make frequently required changes.

## 📲 Sample App

This repo includes a fully functional [example app](/stream-chat-android-ui-components-sample) featuring threads, reactions, typing indicators, optimistic UI updates and offline storage. To run the sample app, start by cloning this repo:

```shell
git clone git@github.com:GetStream/stream-chat-android.git
```

Next, open [Android Studio](https://developer.android.com/studio) and open the newly created project
folder. You'll want to run
the [`stream-chat-android-ui-components-sample`](/stream-chat-android-ui-components-sample) app.

## 🏗️ Jetpack Compose Sample App

We also have a UI implementation built in Jetpack Compose available, currently in a beta version.

This also comes with its own [example app](/stream-chat-android-compose-sample), which you can try
by cloning this repo, and then running the app in
the [`stream-chat-android-compose-sample`](/stream-chat-android-compose-sample) module.

As this is a beta, we're eager to hear your feedback. Join us
in [this repo's discussions](https://github.com/GetStream/stream-chat-android/discussions) or tweet
at us [@getstream_io](https://twitter.com/getstream_io)!

## 💡 Supported features 🎨

Here are some of the features that the SDK supports out-of-the-box:

- Channels list UI
- Channel UI
- Message reactions
- Link previews
- Image, video and file attachments
- Editing and deleting messages
- Typing indicators
- Read indicators
- Push notifications
- Image gallery
- GIF support
- Light and dark themes
- Style customization
- UI customization
- Threads
- Slash commands
- Markdown message formatting
- Unread message counts

For more, see the [SDK's website](https://getstream.io/chat/sdk/android/).

## 🛠️ Installation and Getting Started 🚀

See the [Dependencies](https://getstream.io/chat/docs/sdk/android/basics/dependencies/) and [Getting Started](https://getstream.io/chat/docs/sdk/android/basics/getting-started/) pages of the documentation.

## 💼 We are hiring!

We've recently closed a [\$38 million Series B funding round](https://techcrunch.com/2021/03/04/stream-raises-38m-as-its-chat-and-activity-feed-apis-power-communications-for-1b-users/) and we keep actively growing.
Our APIs are used by more than a billion end-users, and you'll have a chance to make a huge impact on the product within a team of the strongest engineers all over the world.
Check out our current openings and apply via [Stream's website](https://getstream.io/team/#jobs).
