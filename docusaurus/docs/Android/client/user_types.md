---
id: client-user-types
title: User Types
sidebar_position: 4
---

## Connecting with a Regular User

```kotlin
val user = User(
    id = "bender",
    extraData = mutableMapOf(
        "name" to "Bender",
        "image" to "https://bit.ly/321RmWb",
    ),
)

// You can set up a user token in two ways:

// 1. Setup the current user with a JWT token
val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiZmFuY3ktbW9kZS0wIn0.rSnrWOv8EbsiYzJlvVwqwCgATZ1Magj_fZl-bZyCHKI"
client.connectUser(user, token).enqueue { result ->
    if (result.isSuccess) {
        // Logged in
        val user: User = result.data().user
        val connectionId: String = result.data().connectionId
    } else {
        // Handle result.error()
    }
}

// 2. Setup the current user with a TokenProvider
val tokenProvider = object : TokenProvider {
    // Make a request to your backend to generate a valid token for the user
    override fun loadToken(): String = yourTokenService.getToken(user)
}
client.connectUser(user, tokenProvider).enqueue { /* ... */ }
```

## Connecting with a Guest User

Guest sessions can be created client-side and do not require any server-side authentication. Support and livestreams are common use cases for guest users because often you want a visitor to be able to use chat on your application without (or before) they have a regular user account.

Guest users are not available to application using multi-tenancy (teams).

> Unlike anonymous users, guest users are counted towards your MAU usage.

Guest users have a limited set of permissions. You can create a guest user session by using `connectGuestUser` instead of `connectUser`.

```kotlin
client.connectGuestUser(userId = "bender", username = "Bender").enqueue { /*... */ }
```

## Connecting with an Anonymous User

If a user is not logged in, you can call the `connectAnonymousUser` method. While you’re anonymous, you can’t do much, but for the `livestream` channel type, you’re still allowed to read the chat conversation.

```kotlin
client.connectAnonymousUser().enqueue { /*... */ }
```

When you connect to chat using anonymously you receive a special user back with the following data:

```xml
{
	"id": "!anon",
	"role": "anonymous",
	"roles": [],
	"created_at": "0001-01-01T00:00:00Z",
	"updated_at": "0001-01-01T00:00:00Z",
	"last_active": "2020-11-02T18:36:01.125136Z",
	"banned": false,
	"online": true,
	"invisible": false,
	"devices": [],
	"mutes": [],
	"channel_mutes": [],
	"unread_count": 0,
	"total_unread_count": 0,
	"unread_channels": 0,
	"language": ""
}
```
> Anonymous users are not counted toward your MAU number and only have an impact on the number of concurrent connected clients.
