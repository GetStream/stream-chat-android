# Token Provider

1. For tokens without expiration

	```kotlin
	val token = "token"
	client.setUser(ChatUser(), token)
	```

2. For tokens with expiration

	```
	val tokenProvider = object: TokenProvider() {
		fun getToken(listener:(String) -> Unit) {
			//call api and call listener to provide token
			//listener("token")
		}
	} 
	```