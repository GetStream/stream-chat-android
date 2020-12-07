# Stream Chat Client

`Stream Chat Client` is the official low-level Android SDK for [Stream Chat](https://github.com/GetStream/stream-chat-android), a service for building chat and messaging applications. This library supports both Kotlin and Java usage, but we strongly recommend using Kotlin.

This library integrates directly with Stream Chat APIs and does not include UI; if you are interested in a SDK that includes UI components, you should check the stream-chat-android which comes with a collection of re-usable and customizable UI components.

# Related links

* [Chat Tutorial Kotlin](https://getstream.io/tutorials/android-chat/#kotlin) (Uses the low level client as well as custom Views for chat)
* [Stream Kotlin Docs](https://getstream.io/chat/docs/?language=kotlin)
* [Github repo for UX/Views and Sample app](https://github.com/GetStream/stream-chat-android)

## Setup

```groovy
allprojects {
    repositories {
        ...
        mavenCentral()
    }
}
```

```groovy
dependencies {
    implementation "io.getstream:stream-chat-android-client:$stream_version"
}
```

```groovy
android {
  compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
  }
}
```

## Usage
1. Create instance of client

	```kotlin
    val client = ChatClient.Builder(apiKey, context).build()
	```

2. Set user

	```kotlin
    val token = "chat-token"
    val user = User("user-name")
    
    client.setUser(user, token, object : InitConnectionListener() {
        override fun onSuccess(data: ConnectionData) {
            val user = data.user
            val connectionId = data.connectionId
        }

        override fun onError(error: ChatError) {
            val message = error.message
        }
    })
    
    //or
    
    client.setUser(user, token)
    client.subscribeFor(
        ConnectedEvent::class, ErrorEvent::class
    ) { event: ChatEvent ->
         // Handle events
    }
	```

3. Get channels

	```kotlin
    val request = QueryChannelsRequest(FilterObject("type", "messaging"), 0, 100)
    client.queryChannels(request).enqueue { result ->
        val channels = result.data()
    }
	```

4. Send message

	```kotlin
    val channelType = "messaging"
    val channelId = "channel-id"
    val message = Message()
    message.text = "a message"
    client.sendMessage(channelType, channelId, message).enqueue { result ->
        if (result.isSuccess) {
    
        }
    }
	```
	
5. Handle events

	```kotlin
	// either with listener
	client.addSocketListener(object: SocketListener() {
	    //override required methods
	})
	// or with higher level subscription
	// first event delivers socket state: DisconnectedEvent, ConnectingEvent or ConnectedEvent
    client.subscribe { event: ChatEvent ->
        if (event is NewMessageEvent) {
            doSomething()
        }
    }
	```
6. Keep using instance

    ```kotlin
    val client = ChatClient.instance()
    ```
   
# Debugging

To enable logs set log level:

```kotlin
val client = ChatClient.Builder(apiKey, context)
    .logLevel(if (BuildConfig.DEBUG) ChatLogLevel.ALL else ChatLogLevel.NOTHING)
    .build()
```

To intercept log messages add set handler:

```kotlin
val client = ChatClient.Builder(apiKey, context)
    .loggerHandler(object : ChatLoggerHandler {
        override fun logT(throwable: Throwable) {
            throwable.printStackTrace()
        }
    }
    .build()
```

To intercept socket errors:

```kotlin
client.subscribeFor<ErrorEvent> { errorEvent: ErrorEvent ->
    println(errorEvent)
}
```

All SDK log tags have prefix `Chat:`, so to filter out only SDK logs grep the prefix:

```bash
adb logcat your.package.com | grep "Chat:"
```

Set of useful for debugging tags:
- `Chat:Http`
- `Chat:SocketService`
- `Chat:Events`

## Sync / Async
All methods of the library return `Call` object which allows to either `execute` request immediately in the same thread or `enqueue` listener and get result in UI thread:

```kotlin
interface Call<T> {

    @WorkerThread
    fun execute(): Result<T>

    @UiThread
    fun enqueue(callback: (Result<T>) -> Unit = {})

    fun cancel()
    fun <K> map(mapper: (T) -> K): Call<K>
    fun onError(handler: (ChatError) -> Unit): Call<T>
    fun onSuccess(handler: (T) -> Unit): Call<T>
    fun <K> zipWith(call: Call<K>): Call<Pair<T, K>>
    fun <C, B> zipWith(callK: Call<C>, callP: Call<B>): Call<Triple<T, C, B>>
}
```
```kotlin
//sync
val result = client.getMessage("message-id").execute()
//async
client.getMessage("message-id").enqueue { if (it.isSuccess) println(it.data()) }
```

## More

- [Push messages](https://getstream.io/chat/docs/push_android/?language=kotlin)
- [Logging](https://getstream.io/chat/docs/init_and_users/?language=kotlin#setting-the-user)
- [Token Provider](https://getstream.io/chat/docs/tokens_and_authentication/?language=kotlin)
- [Messages](https://getstream.io/chat/docs/send_message/?language=kotlin)

## Development and support

### Naming

- Prefix `Chat` for public classes to avoid spoiling public class name space (generic util classes like `Result` or `Call` are exceptions)
- Postfix `Impl` for interface implementations
- No `m` field name prefix
- `id`, not `ID`
- `t` for `Throwable`
- `interface RetrofitCdnApi`, not `interface RetrofitCDNAPI`
- not `setName`, but `name` with `Builder` classes 

```kotlin
open interface ChatClient {
  fun onError(t:Throwable)
  fun getUserId(): String
}

class ChatClientImpl: ChatClient() {

  private val userId:String = ""

  override fun onError(t:Throwable) {
    t.printStackTrace()
  }
  
  override fun getUserId(): String {
    return userId
  }
} 
```
