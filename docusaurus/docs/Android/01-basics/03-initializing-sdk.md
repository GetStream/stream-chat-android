# Initializing the SDK

## Creating a ChatClient

When integrating with Stream Chat on Android, your first step is initializing the `ChatClient`, which is the main entry point for all operations in the library. `ChatClient` is a singleton: you'll create it once and re-use it across your application.

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
You can access your apiKey in the [Dashboard](https://getstream.io/dashboard).
:::

If you create the `ChatClient` instance following the pattern in the previous example, you will be able to access that instance from any part of your application using the `instance()` method:

```kotlin
class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chatClient = ChatClient.instance() // Returns the singleton instance of ChatClient
    }
}
```

## Creating a ChatDomain

If you want to have offline support or use the UI Components package, you'll need to initialize the `ChatDomain` class in addition to `ChatClient`. You can skip this initialization step if you're only using the low-level client.

The initialization is done similarly to `ChatClient`, using a builder, which exposes several methods for configuring the SDK's behavior:

```kotlin
val chatClient = ChatClient.Builder(apiKey, appContext).build()

val chatDomain = ChatDomain.Builder(appContext, chatClient)
    .offlineEnabled()
    .userPresenceEnabled()
    .build()
```

## Connecting a user

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
To learn more about how to create a token and different user types, see [Connecting a User](../02-client/01-users.md#connecting-a-user).
:::

If the `connectUser` call was successful, you are now ready to use the SDK!
