



Questions:

- testing code that requires async code to complete (think setUser is terrible in android/kotlin)
- Should we call it a StreamChatRepository or something like StreamChatUtility (it's not 100% the same as the standard repository concept)
- Converters can be very verbose is there a way to support List<Any> in one go?
- How to manually trigger an event on the low level client... Or mock the events somehow..

Research

1. How to mock our low level client
2. Figure out Kotlin Coroutine/Livedata bugs and testing issues


## Concepts

Each user has it's own DB
Some of our API responses are user specific. One example is own_reactions on a message. so if you switch users we need to use a different database/storage for the results

Coroutines
Suspend functions are very hard to call in Java.
We return livedata objects and the library only uses coroutines internally.



good reads:

https://codelabs.developers.google.com/codelabs/advanced-android-kotlin-training-testing-basics/

https://codelabs.developers.google.com/codelabs/android-room-with-a-view-kotlin/#0

https://codelabs.developers.google.com/codelabs/advanced-kotlin-coroutines/#0

https://proandroiddev.com/coroutines-with-architecture-components-4c223a51b112

dokku for linting