# stream-chat-android
![Stream Chat](https://imgur.com/dIuKbkh)

[![Build Status](https://travis-ci.com/GetStream/stream-chat-android.svg?branch=master)](https://travis-ci.com/GetStream/stream-chat-android) ![version](https://jitpack.io/v/GetStream/stream-chat-android.svg) [![Component Reference](https://img.shields.io/badge/docs-component%20reference-blue.svg)](https://getstream.github.io/stream-chat-android/)

[stream-chat-android](https://github.com/GetStream/stream-chat-android) is the official Android SDK for [Stream Chat](https://getstream.io/chat), a service for building chat and messaging applications. This library includes both a low level chat SDK and a set of reusable UI components. Most users start out with the UI components, and fall back to the lower level API when they want to customize things.

**Quick Links**

* [Register](https://getstream.io/chat/trial/) to get an API key for Stream Chat
* [Java Chat Tutorial](https://getstream.io/tutorials/android-chat/#java)
* [Kotlin Chat Tutorial](https://getstream.io/tutorials/android-chat/#kotlin)
* [Chat UI Kit](https://getstream.io/chat/ui-kit/)

## Java/Kotlin Chat Tutorial

The best place to start is the [Android Chat Tutorial](https://getstream.io/tutorials/android-chat/#java). It teaches you how to use this SDK and also shows how to make common changes. You can use either [Java](https://getstream.io/tutorials/android-chat/#java) or [Kotlin](https://getstream.io/tutorials/android-chat/#kotlin) depending on your preference.

## Example App

This repo includes a fully functional example app. To run the example app:

```
git clone git@github.com:GetStream/stream-chat-android.git
```

Open the project in Android Studio. Setup your emulator (we're using Pixel 3, API 29 at the moment). Note that the gradle sync process can take some time when you first open the project. 

## Docs

### UI Components

* [Channel List](./docs/ChannelList.md)
* [Message List](./docs/MessageList.md)
* [Message Input](./docs/MessageInput.md)
* [Channel Header](./docs/ChannelHeader.md)

### Chat API

The low level Chat API docs are available for both [Kotlin](https://getstream.io/chat/docs/kotlin/) and [Java](https://getstream.io/chat/docs/java/).
You typically start out by using our UI components, and afterwards build your own as needed using the low level API.

## Installing the Java Chat SDK

- **Step 1** Add repository into root build.gradle

~~~gradle
allprojects {
    repositories {
    ...
    maven {
        url 'https://jitpack.io' }
    }
}
~~~

- **Step 2** Add library dependency into app build.gradle

> See the jitpack badge above for the latest version number

~~~gradle
android {
    ...
    dataBinding {
        enabled = true
    }
	
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation 'com.github.getstream:stream-chat-android:<latest-version>'
}
~~~

## Proguard/R8

If you're using Proguard/R8 you'll want to have a look at the [proguard file we use for the sample](https://github.com/GetStream/stream-chat-android/blob/master/sample/proguard-rules.pro).

## Setup Stream Chat

Make sure to initialize the SDK only once; the best place to do this is in your `Application` class.


```java
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        StreamChat.init("STREAM-API-KEY", getApplicationContext());
    }
}
```

If this is a new Android app you will need to register `BaseApplication` inside AndroidManifest.xml as well. 
```xml
...

<application
    android:name=".BaseApplication"
    ...
>

...

</application>
```

With this you will be able to retrieve the shared Chat client from any part of your application using `StreamChat.getInstance()`. Here's an example:

```java
public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Client client = StreamChat.getInstance(this.getApplication());
		...
	}
```

Make sure that your `AndroidManifest.xml` file include INTERNET and ACCESS_NETWORK_STATE permissions:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="...">

    ... 

    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.INTERNET" />

    ...

</manifest>    
```

### Initialize Chat for a user

1. Retrieve the chat client:

```java
Client client = StreamChat.getInstance(this.getApplication());
```

2. Setup a user, you can use as many custom fields as you want. Both `name` and `image` are used automatically by UI components:

```java
HashMap<String, Object> extraData = new HashMap<>();
extraData.put("name", "Happy Android");
extraData.put("image", "https://bit.ly/2TIt8NR");
User user = new User(USER_ID, extraData);
```

3. Setup chat for current user:

```java
client.setUser(user, USER_TOKEN);
```

The `USER_TOKEN` variable is the unique token for the user with ID `USER_ID` in this case is hard-coded but in real-life it will be something that comes from your auth backend.

Once you called `setUser` you will be able to use Stream Chat APIs; all calls will automatically wait for the `setUser` call to complete. No need to add callbacks or complex syncronization code from your end.

If needed you can add callbacks to `setUser` via `Client.onSetUserCompleted(ClientConnectionCallback)`

```java
client.onSetUserCompleted(new ClientConnectionCallback() {
    @Override
    public void onSuccess(User user) {
    	Log.i(TAG, "user connected!");
	// do some more initialization
    }

    @Override
    public void onError(String errMsg, int errCode) {
    }
});
```

### Retrieve tokens server-side / Handle tokens with expiration

If you generate user tokens with an expiration (`exp` field) you can configure the SDK to request a new token upon expiration.

Here's an example where we make an HTTP call to retrieve a token for current user. 

```java
client.setUser(user, listener -> {
            OkHttpClient httpClient = new OkHttpClient()
                    .newBuilder()
                    .build();

            // request the token for this user
            Request request = new Request.Builder()
                    .url("https://path/to/my/backend/")
                    .header("Authorization", "user-session-id")
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    // the request to get the token failed
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    Log.w(TAG, "getting the token worked!");
                    listener.onSuccess(response.body.string());
                }
            });
        }
);
```

The listener will be called to fetch the user token and once the token is expired; the SDK will retry failed API calls and re-sync history so that no messages are lost during the renewal process.

### Switch users

You can switch from a user to another by calling `disconnect` and `setUser` again. When doing so you are responsible of reloading/re-render the UI. 

This example switches to a different user and calls the `reload` method on the `ChannelListViewModel` when `setUser` is completed.

```java
void switchUser(String userId, String token) {
    Client client = StreamChat.getInstance(getApplication());
    client.disconnect();

    User user = new User(userId);
    client.setUser(user, token);

    viewModel = ViewModelProviders.of(this).get(ChannelListViewModel.class);

    client.onSetUserCompleted(new ClientConnectionCallback() {
        @Override
        public void onSuccess(User user) {
            viewModel.reload();
        }

        @Override
        public void onError(String errMsg, int errCode) {

        }
    });
}
```

## Online status

Connection status to Chat is available via `StreamChat.getOnlineStatus()` which returns a LiveData object you can attach observers to.

```java

StreamChat.getOnlineStatus().observe(...);
```



## Supported features

- Channels list UI
- Channel UI
- Message Reactions
- Link preview
- Images, Videos and Files attachments
- Edit and Delete message
- Typing Inditicators
- Read Inditicators
- Push Notifications
- Image gallery
- GIF support
- Light/Dark themes
- Style customization
- UI customization
- Threads
- Slash commands
- Offline support
- Markdown messages formatting

## Markdown support

Markdown support is based on [Markwon: 4.1.2](https://github.com/noties/Markwon).
Currently SDK doesn't support all `Markwon` features and limited to this plugins:
- [CorePlugin](https://noties.io/Markwon/docs/v4/core/core-plugin.html)
- [LinkifyPlugin](https://noties.io/Markwon/docs/v4/linkify/)
- [ImagesPlugin](https://noties.io/Markwon/docs/v4/image/)
- [StrikethroughPlugin](https://noties.io/Markwon/docs/v4/ext-strikethrough/)
- [TablePlugin](https://noties.io/Markwon/docs/v4/ext-tables/)

If you want to use another library for `Markdown` or extend the `Markwon` plugins you can use the code below
```java
MarkdownImpl.setMarkdownListener((TextView textView, String message)-> {
    // TODO: use your Markdown library or the extended Markwon.
});
```

## Setup custom font

You can set custom fonts for the entire library or for specific UI components.

1. First of all you must put your own font file(s) (.ttf, .otf,…) in your `assets` or `res` folder.

2. Setup for whole library

You can register your custom fonts by `StreamChat.initStyle(StreamChatStyle)`
```java
StreamChat.initStyle(
        new StreamChatStyle.Builder()
                .setDefaultFont(R.font.your_custom_font)
               //.setDefaultFont("fonts/your_custom_font.ttf")
                .build()
```

3. Setup for specific UI components

You can set custom fonts for specific UI components with or without settings for the entire library.  
See font attributes in [UI Components Docs](https://github.com/GetStream/stream-chat-android#ui-components)

## FAQ

### Channel List loading icons spins forever

Not setting the lifecycle owner on a data binding can cause the channel list loading icon to spin forever

```
mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false);
mBinding.setLifecycleOwner(this);
```
### Images are not loaded
In most cases you can try to see the reason in logcat with tag `Glide`. One of the reasons is that app tries to load image from http url, but not https. To fix it you need to define [network security config](https://developer.android.com/training/articles/security-config).
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- Keep in mind this example allows to make any http request, to define proper security config read Android documentation-->
    <base-config cleartextTrafficPermitted="true" />
</network-security-config>
```
And update your `Manifest`:
```xml
<application
	android:networkSecurityConfig="@xml/network_security_config"/>
```
## Getting started

TODO: https://getstream.io/chat/docs/#introduction but with Android code examples
