# Stream Chat Client
`Stream Chat Client` is the official low-level Android SDK for Stream Chat, a service for building chat and messaging applications.

This library integrates directly with Stream Chat APIs and does not include UI; if you are interested in a SDK that includes UI components, you should check the stream-chat-android-core which comes with a collection of re-usable and customizable UI components.

## Setup
```groovy
dependencies {
    implementation 'com.github.getstream:stream-chat-android-client:<latest-version>'
}
```
## Usage
1. Create instance of client

	```kotlin
	val client = StreamChatClient("api-key", "token")
	```

2. Set user

	```kotlin
	client.setUser(ChatUser("id), { result ->
	  if(result.isSuccess())
	    getChannels()
	  else
	    showError(result.error())
	}
	```

3. Get channels

	```kotlin
	fun getChannels() {
	  client.getChannels().enqueue { result -> 
	    if(result.isSuccess()
	      showChannels(result.data())
	    else
	      showError(result.error())
	  }
	}
	```

4. Send message

	```kotlin
	client.sendMessage(channel, ChatMessage("hello"), { result -> 
	  //handle result
	}
	```
	
5. Handle events

	```
	val subscription = client.events().subscribe { event ->
		if(event.type is ChatEvent.NEW_MESSAGE)
		  showNewMessage(event.message)
	}
	subscription.unsubscribe()
	```


## Sync / Async
All methods of the library return `Call` object which allows to either `execute` request immediatly in the same thread or `enqueue` listener and get result in UI thread:

```kotlin
interface Call<T> {
    fun execute(): Result<T>
    fun enqueue(callback: (Result<T>) -> Unit)
    fun cancel()
}
```
```
//sync
val result = client.getChannels().execute()
//async
client.getChannels { result -> showChannels(result) }
```

## Examples

- [Basic async](docs/example-basic-async.md)
- [MVVM + RxJava](docs/example-mvvm-rxjava.md)
- [MVVM + Coroutines](docs/example-mvvm-coroutines.md)
- [MVVM + LiveData](docs/example-mvvm-livedata.md)