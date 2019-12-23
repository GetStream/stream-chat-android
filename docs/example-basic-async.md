# Basic async example
```kotlin
class ChannelsActivity: AppCompatActivity() {
  fun onCreate() {
    val client = StreamChatClient("api-key", "token")
    client.setUser(ChatUser("id"), { result ->
      if(result.isSuccess())
        showChannels(result.data())
      else
        showError(result.error())
    }
  }
  
  fun showChannels(channels:List<ChatChannel>) {
    val adapter = ChannelsAdapter(channels)
    channelsView.setAdapter(adapter)
  }
}
```