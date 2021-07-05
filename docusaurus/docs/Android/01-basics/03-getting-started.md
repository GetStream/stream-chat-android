# Getting Started

Let's see how you can get started with the Android Chat SDK after adding the required [dependencies](./02-dependencies.md). This page tells you what steps you should perform in your app to initialize the Stream Chat SDK.

If you're looking for a complete, step-by-step guide that includes setting up an Android project from scratch, try the [Android In-App Messaging Tutorial](https://getstream.io/tutorials/android-chat/) instead.

### Creating a ChatClient

Your first step is initializing the `ChatClient`, which is the main entry point for all operations in the library. `ChatClient` is a singleton: you'll create it once and re-use it across your application.

A best practice is to initialize `ChatClient` in the `Application` class:

 ```kotlin
 class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val chatClient = ChatClient.Builder("apiKey", context).build()
    }
}
 ```

The _Builder_ for `ChatClient` exposes configuration options for features such as [Logging](./05-logging.md).

:::note
To generate an API key, you can sign up for a [free 30-day trial](https://getstream.io/chat/trial/). You can then access your api key in the [Dashboard](https://getstream.io/dashboard).
:::

If you create the `ChatClient` instance following the pattern in the previous example, you will be able to access that instance from any part of your application using the `instance()` method:

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chatClient = ChatClient.instance() // Returns the singleton instance of ChatClient
    }
}
```

### Creating a ChatDomain

If you want to have offline support or use the UI Components package, you'll need to initialize the `ChatDomain` class in addition to `ChatClient`. You can skip this initialization step if you're only using the low-level client.

The initialization is done similarly to `ChatClient`, using a builder, which exposes several methods for configuring the SDK's behavior:

```kotlin
val chatClient = ChatClient.Builder(apiKey, appContext).build()

val chatDomain = ChatDomain.Builder(appContext, chatClient)
    .offlineEnabled()
    .userPresenceEnabled()
    .build()
```

For more about working with `ChatDomain`, see [Working with Offline](../02-client/06-guides/06-working-with-offline.md).

### Connecting a User

The next step is connecting the user. This requires a valid Stream Chat token. As you must use your `API_SECRET` to create this token, it is unsafe to generate this token outside of a secure server.

```kotlin
val user = User(
    id = "bender",
    extraData = mutableMapOf(
        "name" to "Bender",
        "image" to "https://bit.ly/321RmWb",
    ),
)

ChatClient.instance().connectUser(user = user, token = "userToken") // Replace with a real token
    .enqueue { result ->
        if (result.isSuccess) {
            // Handle success
        } else {
            // Handle error
        }
    }
```

:::note
To learn about how to create a token and different user types, see [Tokens & Authentication](https://getstream.io/chat/docs/android/tokens_and_authentication/?language=kotlin).
:::

If the `connectUser` call was successful, you are now ready to use the SDK! 🎉

The methods of the `ChatClient` class allow you to create channels, send messages, add reactions, and perform many more low-level operations. You can also use the SDK's pre-built UI Components that come with ViewModels that will perform data fetching and sending for you, as described below.

### Adding UI Components

To add the UI Components to your application, see the [Get Started](../03-ui/01-getting-started.md) page of the UI Components section first, which explains how the components work.

Then you can proceed to our guides that show you how to [build a channel list screen](../03-ui/04-guides/01-building-channel-list-screen.md) and [build a message list screen](../03-ui/04-guides/02-building-message-list-screen.md).
