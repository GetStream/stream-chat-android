# Localizing SDK strings

Stream SDK is available with US English string resources out-of-the-box. The solution for adding your own localization is based on standard [Android mechanism](https://developer.android.com/guide/topics/resources/localization) of switching resources on system Locale change. You can provide custom localization for the SDK's string resources by overriding them in the locale-specific `/res/values` directories of your project.
 

:::note
All of the string resources names provided by Stream SDK are prefixed with `stream_ui_`, e.g. `<string name="stream_ui_message_list_empty">No messages</string>`.
:::

### Setting up custom translations

In this guide we are going to implement a custom translation for ChannelListHeaderView UI component. We are going to provide Polish language translation.

Usually, base string resources are located in the `/res/values/strings.xml` file. In order to add a translations for the new language (PL) we are going to create a new `strings.xml` file under `res/values-pl` directory.

Let's take a look at the [strings_channel_list_header.xml](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-ui-components/src/main/res/values/strings_channel_list_header.xml) file and discover strings defined for `ChannelListHeaderView`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="stream_ui_channel_list_header_connected">Stream Chat</string>
    <string name="stream_ui_channel_list_header_disconnected">Waiting for network</string>
</resources>

```

As you can see there are 2 string resources used by this UI component. Let's say we need to localize only the one called `stream_ui_channel_list_header_disconnected`. 
In order to do it, we need to add the following string translation to the target locale-specific file. In our case this will be the `res/values-pl/strings.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="stream_ui_channel_list_header_disconnected">Oczekiwanie na połączenie</string>
</resources>
``` 

As the result, your app will display the base _Waiting for network_ text for all of the languages except for Polish (PL). In this case the translated text will be shown.

:::note
String resources in SDK are grouped in the resource files, usually prefixed with `strings_`. Each file corresponds to a related UI component or specific usage. You can browse them in order to provide translations:
* [strings_attachment_gallery.xml](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-ui-components/src/main/res/values/strings_attachment_gallery.xml) 
* [strings_channel_list.xml](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-ui-components/src/main/res/values/strings_channel_list.xml) 
* [strings_channel_list_header.xml](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-ui-components/src/main/res/values/strings_channel_list_header.xml) 
* [strings_mention_list.xml](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-ui-components/src/main/res/values/strings_mention_list.xml) 
* [strings_message_input.xml](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-ui-components/src/main/res/values/strings_message_input.xml)
* [strings_message_list.xml](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-ui-components/src/main/res/values/strings_message_list.xml) 
* [strings_message_list_header.xml](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-ui-components/src/main/res/values/strings_message_list_header.xml) 
* [strings_search.xml](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-ui-components/src/main/res/values/strings_search.xml) 
* [strings_common.xml](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-ui-components/src/main/res/values/strings_common.xml)
* [strings.xml](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android/src/main/res/values/strings.xml)
:::
