---
id: clientConnectingUser
title: Connecting the User
sidebar_position: 3
---

The next step is connecting the user. A valid `StreamChat` token is all you need to properly set your current app user as the current user of `ChatClient`. This token can't be created locally and it must be provided by your backend.

```kotlin
val user = User(
    id = "bender",
    extraData = mutableMapOf(
        "name" to "Bender",
        "image" to "https://bit.ly/321RmWb",
    ),
)

ChatClient.instance().connectUser(user = user, token = "userToken")
    .enqueue { result ->
        if (result.isSuccess) {
            // Handle success
        } else {
            // Handle error
    }
}
```
