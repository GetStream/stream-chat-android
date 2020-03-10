![latest-version](https://jitpack.io/v/GetStream/stream-chat-android-client.svg)

# Stream Chat Client

Note: This is an updated version of the Stream low level chat client. It's a preview and not yet suitable for production usage. Right now we recommend using the regular Java based SDK.

`Stream Chat Client` is the official low-level Android SDK for Stream Chat, a service for building chat and messaging applications.

This library integrates directly with Stream Chat APIs and does not include UI; if you are interested in a SDK that includes UI components, you should check the stream-chat-android-core which comes with a collection of re-usable and customizable UI components.


## Improvements

- Use `StreamChatClient` instead of `Client` to avoid polluting the namespace and avoid auto-import to get confused
- Let the developer choose between sync and async for API calls (right now sync is not even possible)
- Minimal list of external dependencies
- Async style API calls are simplified, only 1 function which receives `Result<T>`. Errors are now of type `ClientError` which is a throwable (more useful and common to work with)

## Setup
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
```groovy
dependencies {
    implementation 'com.github.getstream:stream-chat-android-client:latest-version'
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
    client.setUser(User("user-name"), token, object : InitConnectionListener() {
        override fun onSuccess(data: ConnectionData) {
            val user = data.user
            val connectionId = data.connectionId
        }

        override fun onError(error: ChatError) {
            val message = error.message
        }
    })
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
	// or with observable instance
    client.events().subscribe {
       if(it is NewMessageEvent) doSomething()
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

All SDK log tags have prefix `Chat:`, so to filter out only SDK logs grep the prefix:

```bash
adb logcat your.package.com | grep "Chat:"
```

Set of useful for debugging tags:
- `Chat:Http`
- `Chat:SocketService`
- `Chat:Events`

## Sync / Async
All methods of the library return `ChatCall` object which allows to either `execute` request immediately in the same thread or `enqueue` listener and get result in UI thread:

```kotlin
interface Call<T> {
    fun execute(): Result<T>
    fun enqueue(callback: (Result<T>) -> Unit)
    fun cancel()
    fun <K> map(mapper: (T) -> K): Call<K>
    fun onError(handler: (ChatError) -> Unit): Call<T>
    fun onNext(handler: (T) -> Unit): Call<T>
    fun <K> zipWith(call: Call<K>): Call<Pair<T, K>>
    fun <C, B> zipWith(callK: Call<C>, callP: Call<B>): Call<Triple<T, C, B>>
}
```
```kotlin
//sync
val result = client.queryChannels().execute()
//async
client.getChannels { result -> showChannels(result) }
```

## More

- [Client life cycle](docs/client-lifecycle.md)
- [Push messages](docs/push-messages.md)
- [Logging](docs/logging.md)
- [Token Provider](docs/token-provider.md)
- SDK architecture

## Examples

- [Basic async](docs/example-basic-async.md)
- [MVVM + RxJava](docs/example-mvvm-rxjava.md)
- [MVVM + Coroutines](docs/example-mvvm-coroutines.md)
- [MVVM + LiveData](docs/example-mvvm-livedata.md)
- [Pagination: channels](docs/example-pagination-channels.md)
- [Pagination: messages](docs/example-pagination-messages.md)

## Development and support

### Naming

- Prefix `Chat` for public classes to avoid spoiling public class name space (generic util classes like `Result` or `Call` are exceptions)
- Postix `Impl` for interface implementations
- No `m` field name prefix
- `id`, not `ID`
- `t` for `Throwable`
- `interface RetrfitCdnApi`, not `interface RetrofitCDNAPI`
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
