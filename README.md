# Official Android SDK for [Stream Chat](https://getstream.io/chat/sdk/android/)

<p align="center">
  <a href="https://getstream.io/tutorials/android-chat/">
    <img src="/docs/sdk-hero-android.png"/>
  </a>
</p>

<p align="center">
  <a href="https://github.com/GetStream/stream-chat-android/actions/workflows/build-and-test.yml"><img src="https://github.com/GetStream/stream-chat-android/workflows/Build%20and%20test/badge.svg" /></a>
  <a href="https://android-arsenal.com/api?level=21"><img alt="API" src="https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat"/></a>
  <a href="https://github.com/GetStream/stream-chat-android/releases"><img src="https://img.shields.io/github/v/release/GetStream/stream-chat-android" /></a>
</p>

<div align="center">

![stream-chat-android-client](https://img.shields.io/badge/stream--chat--android--client-3.16%20MB-lightgreen)
![stream-chat-android-offline](https://img.shields.io/badge/stream--chat--android--offline-3.37%20MB-lightgreen)
![stream-chat-android-ui-components](https://img.shields.io/badge/stream--chat--android--ui--components-7.91%20MB-lightgreen)
![stream-chat-android-compose](https://img.shields.io/badge/stream--chat--android--compose-9.96%20MB-lightgreen)

</div>

> **Note:** The SDK sizes reflect the maximum possible addition if none of their internal dependencies are already in your app. In most cases, the actual impact will be smaller.

This is the official Android SDK for [Stream Chat](https://getstream.io/chat/sdk/android/), a service for building chat and messaging applications. This library includes both a low-level chat SDK and a set of reusable UI components. Most users start with the UI components, and fall back to the lower level API when they want to customize things.

We're proud to say that we're the first Android Chat SDK that supports Jetpack Compose! We [released](https://github.com/GetStream/stream-chat-android/releases/tag/4.15.0) our Compose UI Components one day after the official Jetpack Compose 1.0 release and our team members have been working hard on it since then.

Now it's mature and stable enough for us to officially recommend it for all new applications and all modern chat implementations. If you're looking for something highly customizable and extremely performant, check out our [Compose SDK](https://getstream.io/chat/sdk/compose/).

The Android SDK supports both Kotlin and Java, but *we strongly recommend using Kotlin*.

> **Note**: The Compose SDK supports only Kotlin, since Compose uses Kotlin compiler plugins to process the UI.

### 🔗 Quick Links

* [Register](https://getstream.io/chat/trial/): Create an account and get an API key for Stream Chat
* [Chat Tutorial](https://getstream.io/tutorials/android-chat/#kotlin): Learn the basics of the SDK by by building a simple messaging app (Kotlin or Java)
* [UI Components sample app](/stream-chat-android-ui-components-sample): Full messaging app with threads, reactions, optimistic UI updates and offline storage
* [Compose UI Components sample app](/stream-chat-android-compose-sample): Messaging sample app built with Jetpack Compose!
* [Client Documentation](https://getstream.io/chat/docs/android/?language=kotlin)
* [UI Components Documentation](https://getstream.io/chat/docs/sdk/android/)
* [Compose UI Components Documentation](https://getstream.io/chat/docs/sdk/android/compose/overview/)
* [API docs](https://getstream.github.io/stream-chat-android/): Full generated docs from Dokka
* [Jetpack Compose Planning](https://github.com/orgs/GetStream/projects/6): Jetpack Compose public project management board and milestone overview

## 👩‍💻 Free for Makers 👨‍💻

Stream is free for most side and hobby projects. To qualify, your project/company needs to have < 5 team members and < $10k in monthly revenue.
For complete pricing details, visit our [Chat Pricing Page](https://getstream.io/chat/pricing/).

## 🗺️ Overview and Documentation 📚

This SDK consists of two low-level artifacts you can build on:

- [**Client**](/stream-chat-android-client): A low-level client for making API calls and receiving chat events.
    - [Documentation website](https://getstream.io/chat/docs/android/?language=kotlin)
- [**Offline support**](/stream-chat-android-offline): Local caching and automatic retries, exposed via Flow and LiveData APIs.
    - [Offline Support](https://getstream.io/chat/docs/sdk/android/client/guides/offline-support/)

**We also have two UI SDKs**. You can use our Compose UI Components SDK, or if you're using older UI solutions, the XML-based UI Components:

- [**Compose UI Components**](/stream-chat-android-compose): Reusable and modular Composables for displaying conversations, lists of channels, and more!
  - [Sample app](/stream-chat-android-compose-sample)
  - [Documentation](https://getstream.io/chat/docs/sdk/android/compose/overview/)
  - [Compose SDK Guidelines](/stream-chat-android-compose/GUIDELINES.md)
- [**UI Components**](/stream-chat-android-ui-components): Reusable and customizable chat Views for displaying conversations, lists of channels, and more!
  - [Sample app](/stream-chat-android-ui-components-sample)
  - [Documentation](https://getstream.io/chat/docs/sdk/android/ui/overview/)

Learn more about the modules by visiting [the documentation](https://getstream.io/chat/docs/sdk/android/).

## 📖 Tutorial

The best place to start is the [Compose Chat Messaging Tutorial](https://getstream.io/chat/compose/tutorial/). It teaches you the basics of using the Compose Chat SDK and also shows how to make frequently required changes.

>  **Note**: If you're using older UI toolkits, like XML, you can follow the [Android Chat Messaging Tutorial](https://getstream.io/tutorials/android-chat/) which features the XML-based UI Components.

## 🛠️ Installation and Getting Started 🚀

See the [Dependencies](https://getstream.io/chat/docs/sdk/android/basics/dependencies/) and [Getting Started](https://getstream.io/chat/docs/sdk/android/client/overview/) pages of the documentation.

## 🔮 Sample Apps

### Compose Sample App

Our Jetpack Compose implementation comes with its own [example app](/stream-chat-android-compose-sample), which you can play with to see how awesome Compose is.

To run the sample app, start by cloning this repo:

```shell
git clone git@github.com:GetStream/stream-chat-android.git
```

Next, open [Android Studio](https://developer.android.com/studio) and open the newly created project folder. You'll want to run the [`stream-chat-android-compose-sample`](/stream-chat-android-compose-sample) module.

Since Compose is a highly customizable SDK, we're eager to hear your feedback on how it helps you build complex Chat UI. Join us in [this repo's discussions](https://github.com/GetStream/stream-chat-android/discussions) or tweet at us [@getstream_io](https://twitter.com/getstream_io)!

### Sample App

However, if you're still using XML due to technical limitations, our UI Components SDK includes a fully functional [example app](/stream-chat-android-ui-components-sample) featuring threads, reactions, typing indicators, optimistic UI updates and offline storage. To run the sample app, start by cloning this repo:

```shell
git clone git@github.com:GetStream/stream-chat-android.git
```

Next, open [Android Studio](https://developer.android.com/studio) and open the newly created project folder. You'll want to run the [`stream-chat-android-ui-components-sample`](/stream-chat-android-ui-components-sample) app.

### Other Sample Apps

We also maintain a dedicated repository for fully-fledged sample applications at [GetStream/Android-Samples](https://github.com/GetStream/Android-Samples).

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

## 🛠️ R8 / ProGuard

When utilizing **R8**, the rules for shrinking and obfuscation are applied automatically.

If you are using **ProGuard**, you will need to add the following rules from [client](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-client/consumer-proguard-rules.pro), [ui-common](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-ui-common/consumer-proguard-rules.pro), and [previewdata](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-previewdata/consumer-proguard-rules.pro) modules to your application.

You might also need apply rules for [Coroutines](https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/resources/META-INF/proguard/coroutines.pro), [Retrofit](https://github.com/square/retrofit/blob/master/retrofit/src/main/resources/META-INF/proguard/retrofit2.pro) and [OkHttp](https://github.com/square/okhttp/blob/master/okhttp/src/jvmMain/resources/META-INF/proguard/okhttp3.pro) which are dependencies of the SDK.

## 💼 We are hiring!

We've recently closed a [\$38 million Series B funding round](https://techcrunch.com/2021/03/04/stream-raises-38m-as-its-chat-and-activity-feed-apis-power-communications-for-1b-users/) and we keep actively growing.
Our APIs are used by more than a billion end-users, and you'll have a chance to make a huge impact on the product within a team of the strongest engineers all over the world.
Check out our current openings and apply via [Stream's website](https://getstream.io/team/#jobs).

## License

```
Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.

Licensed under the Stream License;
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   https://github.com/GetStream/stream-chat-android/blob/main/LICENSE

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```