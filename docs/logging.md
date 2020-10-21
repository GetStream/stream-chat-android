# Logging

```kotlin
val client = StreamChatClient.Builder()
	.loggingLevel(BuildConfig.DEBUG ? ALL : NOTHING)
	.loggerHandler(object: LoggerHandler() {
		override fun logExeption(t:Throwable) {
			//log exception
		}
	})
```