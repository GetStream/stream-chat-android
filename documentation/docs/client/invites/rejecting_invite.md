---
id: RejectingInvite
title: Rejecting an invite
sidebar_position: 3
---
To reject an invite, call the `rejectInvite` method. This method does not require a user ID as it pulls the user ID from the current session in store from the `connectUser` call.

```kotlin
channelClient.rejectInvite().enqueue { result ->  
    if (result.isSuccess) {  
        // Invite rejected  
    } else {  
        // Handle result.error()  
    }  
}
```
