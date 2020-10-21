# Issues
# Mentioned user in message
Backend api endpoint for sending message accepts message object with `mentioned_users` field with array of user string ids, returns message object with the same field, but with different type: array of user objects. `Message` model handles it:
```kotlin
@IgnoreDeserialisation
@SerializedName("mentioned_users")
var mentionedUsersIds: MutableList<String> = mutableListOf(),

@IgnoreSerialisation
@SerializedName("mentioned_users")
var mentionedUsers: MutableList<User> = mutableListOf(),
```
Hence to send message with mentioned user `mentionedUsersIds` must be used:
```kotlin
val message = Message(text = "hello")
message.mentionedUsersIds.add("some-user-id")
client.sendMessage("messagin", "channel-id", message).enqueue { messageResult ->

}
```