# Stream Chat Android UI Components

This module contains reusable UI components to use in combination with the [offline support](../stream-chat-android-offline) library. See [the main README](../README.md) in this repo for more info.

This library supports both Kotlin and Java usage, but we strongly recommend using Kotlin.

<p align="center">
  <img alt="Channels screen" src="../docs/sample-channels-light.png" width="40%">
&nbsp; &nbsp;
  <img alt="Messages screen" src="../docs/sample-messages-light.png" width="40%">
</p>

## Setup

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "io.getstream:stream-chat-android-ui-components:$stream_version"
}
```

> For the latest version, check the [Releases page](https://github.com/GetStream/stream-chat-android/releases).

## Sample app

To see these components in action, check out our [sample app](../stream-chat-android-ui-components-sample), which implements a fully-featured messaging application based on these components.

## UI components

You'll find the detailed documentation for the components on our website:

- [Channel List View](https://getstream.io/chat/docs/android/channel_list_view/?language=kotlin)
- [Channel List Header View](https://getstream.io/chat/docs/android/channel_list_header_view/?language=kotlin)
- [Message List View](https://getstream.io/chat/docs/android/message_list_view/?language=kotlin)
- [Message List Header View](https://getstream.io/chat/docs/android/message_list_header_view/?language=kotlin)
- [Message Input View](https://getstream.io/chat/docs/android/message_input_view/?language=kotlin)
- [Search Input View](https://getstream.io/chat/docs/android/search_input_view/?language=kotlin)
- [Search Result List View](https://getstream.io/chat/docs/android/search_result_list_view/?language=kotlin)
- [Attachment Gallery](https://getstream.io/chat/docs/android/attachmentgallery/?language=kotlin)
