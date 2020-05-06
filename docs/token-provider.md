# Token Provider

1. For tokens without expiration

	```kotlin
	val token = "token"
	client.setUser(User("id"), token)
	```

2. For tokens with expiration

    ```kotlin
    val tokenProvider = object: TokenProvider() {
        // Executed in background thread   
        override fun loadToken(): String {
            return api.getChatToken().execute()
        }
    }
    client.setUser(User("id"), tokenProvider)
    ```
