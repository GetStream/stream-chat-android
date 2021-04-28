---
id: clientReactions
title: Reactions
sidebar_position: 18
---

Stream Chat has built-in support for user Reactions. Common examples are likes, comments, loves, etc. Reactions can be customized so that you are able to use any type of reaction your application requires.

Similar to other objects in Stream Chat, reactions allow you to add custom data to the reaction of your choice. This is helpful if you want to customize the reaction logic.

| Name | Type | Description | Default | Optional |
| :--- | :--- | :--- | :--- | :--- |
| message_id | string | ID of the message to react to | | |
| reaction | object | Reaction object | | |
| reaction.type | string | 	Type of the reaction. User could have only 1 reaction of each type per message | | |
| reaction.score | integer | Score of the reaction for cumulative reactions (see example below) | 1 | &check; |
| user_id | string | User ID for server side calls | | &check; |
| enforce_unique | boolean | If set to true, new reaction will replace all reactions the user has (if any) on this message | false | &check; |

> :warning: Custom data for reactions is limited to 1KB.

## Sending a Reaction

```kotlin
val channelClient = client.channel("messaging", "general") 
 
val reaction = Reaction( 
    messageId = "message-id", 
    type = "like", 
    score = 1,
    extraData = mutableMapOf("customField" to 1)
) 
channelClient.sendReaction(reaction).enqueue { result -> 
    if (result.isSuccess) { 
        val sentReaction: Reaction = result.data() 
    } else { 
        // Handle result.error() 
    } 
} 
```

## Replacing a Reaction

```kotlin
// Add reaction 'like' and replace all other reactions of this user by it
channelClient.sendReaction(reaction, enforceUnique = true).enqueue { result -> 
    if (result.isSuccess) { 
        val sentReaction = result.data() 
    } else { 
        // Handle result.error() 
    } 
}
```

## Deleting a Reaction

```kotlin
channelClient.deleteReaction( 
    messageId = "message-id", 
    reactionType = "like", 
).enqueue { result -> 
    if (result.isSuccess) { 
        val message = result.data() 
    } else { 
        // Handle result.error() 
    } 
}
```

## Paginating Reactions

Messages returned by the APIs automatically include the 10 most recent reactions. You can also retrieve more reactions and paginate using the following logic:

```kotlin
// Get the first 10 reactions 
channelClient.getReactions( 
    messageId = "message-id", 
    offset = 0, 
    limit = 10, 
).enqueue { result -> 
    if (result.isSuccess) { 
        val reactions: List<Reaction> = result.data() 
    } else { 
        // Handle result.error() 
    } 
} 
 
// Get the second 10 reactions 
channelClient.getReactions( 
    messageId = "message-id", 
    offset = 10, 
    limit = 10, 
).enqueue { /* ... */ } 
 
// Get 10 reactions after particular reaction 
channelClient.getReactions( 
    messageId = "message-id", 
    firstReactionId = "reaction-id", 
    limit = 10, 
).enqueue { /* ... */ }
```

## Cumulative (Clap) Reactions

You can use the Reactions API to build something similar to Medium's clap reactions. If you are not familiar with this, Medium allows you to clap articles more than once and shows the sum of all claps from all users.

To do this, you only need to include a score for the reaction (ie. user X clapped 25 times) and the API will return the sum of all reaction scores as well as each user individual scores (ie. clapped 475 times, user Y clapped 14 times).

```kotlin
val reaction = Reaction(messageId = "message-id", type = "clap", score = 5) 
channelClient.sendReaction(reaction).enqueue { /* ... */ }
```
