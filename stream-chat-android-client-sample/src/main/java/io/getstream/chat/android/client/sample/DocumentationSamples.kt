@file:Suppress("UNUSED_VARIABLE", "MayBeConstant")

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.api.models.SearchMessagesRequest
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationMutesUpdatedEvent
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Filters.and
import io.getstream.chat.android.client.models.Filters.contains
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.getTranslation
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.Result
import java.io.File
import java.util.ArrayList

val client = ChatClient.instance()
val channelType = ""
val channelId = ""
val cid = ""
val messageId = ""
val message = Message()
val channelController = client.channel(channelType, channelId)
val context: Context = null!!
val parentMessageId = ""
val firstMessageId = ""
val userId = ""
val user = User()
val token = ""

fun getApplicationContext(): Context {
    return null!!
}

fun init() {
    // Typically done in your Application class
    val client = ChatClient.Builder("{{ api_key }}", context).build()

    // Static reference to initialised client
    // val client = ChatClient.instance()
}

fun setUser() {

    val user = User("user-id")
    val token = "{{ chat_user_token }}"

    user.extraData["name"] = "Bender"
    user.extraData["image"] = "https://bit.ly/321RmWb"

    client.setUser(
        user,
        token,
        object : InitConnectionListener() {
            override fun onSuccess(data: ConnectionData) {
                val user = data.user
                val connectionId = data.connectionId
            }

            override fun onError(error: ChatError) {
                error.cause?.printStackTrace()
            }
        }
    )
}

/**
 * https://getstream.io/nessy/docs/chat_docs/channels/channel_conversations
 */
internal object OneToOneConversations {
    fun creatingConversations() {
        val members = listOf("thierry", "tomasso")
        channelController.create(members).enqueue {
            val newChannel = it.data()
        }
    }
}

fun channel() {

    val channelController = client.channel(channelType, channelId)
    val extraData = mutableMapOf<String, Any>()

    extraData["name"] = "Talking about life"

    // watching a channel"s state
    // note how the withWatch() argument ensures that we are watching the channel for any changes/new messages
    val request = QueryChannelRequest()
        .withData(extraData)
        .withMessages(20)
        .withWatch()

    channelController.query(request).enqueue {
        if (it.isSuccess) {
            val channel = it.data()
        } else {
            it.error().cause?.printStackTrace()
        }
    }
}

fun sendMessage() {

    // prepare the message
    val message = Message()
    message.text = "hello world"

    // send the message to the channel
    channelController.sendMessage(message).enqueue {
        if (it.isSuccess) {
            val message = it.data()
        } else {
            it.error().cause?.printStackTrace()
        }
    }
}

fun events() {
    val disposable = client.subscribe { event ->
        if (event is NewMessageEvent) {
            val message = event.message
        }
    }
    disposable.dispose()
}

fun initClient() {
    // Typically done in your Application class

    val apiKey = "{{ api_key }}"
    val token = "{{ chat_user_token }}"
    val context = getApplicationContext()
    val client = ChatClient.Builder(apiKey, context).build()

    // Set the user to establish the websocket connection
    // Usually done when you open the chat interface
    // extraData allows you to add any custom fields you want to store about your user
    // the UI components will pick up name and image by default

    val user = User("bender")
    user.extraData["image"] = "https://bit.ly/321RmWb"
    user.extraData["name"] = "Bender"

    client.setUser(
        user,
        token,
        object : InitConnectionListener() {

            override fun onSuccess(data: ConnectionData) {
                val user = data.user
                val connectionId = data.connectionId
            }

            override fun onError(error: ChatError) {
                error.cause?.printStackTrace()
            }
        }
    )
}

fun setGuestUser() {
    val userId = "user-id"
    val userName = "bender"
    client.getGuestToken(userId, userName).enqueue {
        val token = it.data().token
        val user = it.data().user

        client.setUser(user, token)
    }
}

fun setAnon() {
    client.setAnonymousUser()
}

fun switch() {
    client.disconnect()
    client.setUser(User("bender"), "{{ chat_user_token }}")
}

fun queryUsers1() {

    val filter = Filters.`in`("id", listOf("john", "jack", "jessie"))
    val offset = 0
    val limit = 10
    val request = QueryUsersRequest(filter, offset, limit)

    client.queryUsers(request).enqueue {
        val users = it.data()
    }
}

fun queryUsers2() {
    val filter = Filters.`in`("id", listOf("jessica"))
    val offset = 0
    val limit = 10
    val sort = QuerySort<User>().desc(User::lastActive)

    val request = QueryUsersRequest(filter, offset, limit, sort)

    client.queryUsers(request).enqueue {
        val users = it.data()
    }
}

fun sendMessage2() {
    val message = Message()
    message.text =
        "Josh I told them I was pesca-pescatarian. Which is one who eats solely fish who eat other fish."
    message.extraData["anotherCustomField"] = 234

    // add an image attachment to the message
    val attachment = Attachment()
    attachment.type = "image"
    attachment.imageUrl = "https://bit.ly/2K74TaG"
    attachment.fallback = "test image"
    // add some custom data to the attachment
    attachment.extraData["myCustomField"] = 123

    message.attachments.add(attachment)

    // include the user id of the mentioned user
    // message.mentionedUsers.add(User("josh-id"))

    channelController.sendMessage(message).enqueue {
        val message = it.data()
    }
}

fun getMessage() {
    channelController.getMessage("message-id").enqueue {
        val message = it.data()
    }
}

fun updateMessage() {
    // update some field of the message
    message.text = "my updated text"
    // send the message to the channel
    channelController.updateMessage(message).enqueue {
        val message = it.data()
    }
}

fun deleteMessage() {
    channelController.deleteMessage(messageId).enqueue {
        val deletedMessage = it.data()
    }
}

fun sendMessage3() {
    val message = Message()
    message.text = "Check this bear out https://imgur.com/r/bears/4zmGbMN"

    // send the message to the channel
    channelController.sendMessage(message).enqueue {
        val sentMessage = it.data()
    }
}

fun sendFileAndImage() {

    val imageFile = File("path")
    val anyOtherFile = File("path")

    // upload an image
    channelController.sendImage(
        imageFile,
        object : ProgressCallback {
            override fun onSuccess(file: String) {
            }

            override fun onError(error: ChatError) {
            }

            override fun onProgress(progress: Long) {
            }
        }
    )

    // upload a file
    channelController.sendFile(
        anyOtherFile,
        object : ProgressCallback {
            override fun onSuccess(file: String) {
            }

            override fun onError(error: ChatError) {
            }

            override fun onProgress(progress: Long) {
            }
        }
    )
}

fun sendReaction() {
    val score = 5
    val reaction = Reaction("message-id", "like", score)

    channelController.sendReaction(reaction).enqueue {
        val reaction = it.data()
    }
}

fun deleteReaction() {
    channelController.deleteReaction("message-id", "like").enqueue {
        val message = it.data()
    }
}

fun getReactions() {
    // get the first 10 reactions
    channelController.getReactions(messageId, 0, 10).enqueue {
        val reactions = it.data()
    }

    // get the second 10 reactions
    channelController.getReactions(messageId, 10, 10).enqueue {
        val reactions = it.data()
    }

    // get 10 reactions after particular reaction
    val reactionId = "reaction-id"
    channelController.getReactions(messageId, reactionId, 10).enqueue {
        val reactions = it.data()
    }
}

fun sendParentMessage() {

    // set the parent id to make sure a message shows up in a thread
    val parentMessage = Message()
    val message = Message()
    message.text = "hello world"
    message.parentId = parentMessage.id

    // send the message to the channel
    channelController.sendMessage(message).enqueue {
        val message = it.data()
    }
}

fun getReplies() {
    val limit = 20
    // retrieve the first 20 messages inside the thread
    client.getReplies(parentMessageId, limit).enqueue {
        val replies = it.data()
    }
    // retrieve the 20 more messages before the message with id "42"
    client.getRepliesMore(parentMessageId, "42", limit).enqueue {
        val replies = it.data()
    }
}

fun search() {

    val offset = 0
    val limit = 10
    val query = "supercalifragilisticexpialidocious"

    val messageFilter = Filters.`in`("members", listOf("john"))
    val channelFilter = Filters.eq("type", "messaging")

    client.searchMessages(
        SearchMessagesRequest(
            offset,
            limit,
            channelFilter,
            messageFilter
        )
    ).enqueue {
        val messages = it.data()
    }
}

fun listeningSomeEvent() {
    val disposable = channelController
        .subscribeFor("message.deleted") { messageDeletedEvent ->
        }
    disposable.dispose()
}

fun listenAllEvents() {
    val disposable = channelController.subscribe { event ->
        if (event is NewMessageEvent) {
        }
    }
    disposable.dispose()
}

fun connectionEvents() {
    client.subscribe { event ->
        when (event) {
            is ConnectedEvent -> {
                // socket is connected
            }
            is ConnectingEvent -> {
                // socket is connecting
            }
            is DisconnectedEvent -> {
                // socket is disconnected
            }
        }
    }
}

fun stopListening() {
    val disposable = channelController.subscribe { event -> }
    disposable.dispose()
}

fun notificationEvents() {
    channelController
        .subscribeFor("notification.added_to_channel") { notificationEvent ->
            notificationEvent
        }
}

fun createChannelController() {

    val channelController = client.channel(channelType, channelId)

    val extraData = mutableMapOf<String, Any>()
    val members: MutableList<String> = ArrayList()

    extraData["name"] = "Founder Chat"
    extraData["image"] = "http://bit.ly/2O35mws"

    members.add("thierry")
    members.add("tommaso")

    channelController
        .create(members, extraData)
        .enqueue { result ->
            if (result.isSuccess) {
                val channel = result.data()
            } else {
                val error = result.error()
            }
        }
}

fun watch() {
    channelController.watch().enqueue {
        val channel = it.data()
    }
}

fun stopWathing() {
    channelController.stopWatching().enqueue {
        val channel = it.data()
    }
}

fun queryChannels() {
    val filter = Filters
        .`in`("members", "thierry")
        .put("type", "messaging")
    val offset = 0
    val limit = 10
    val sort = QuerySort<Channel>().desc(Channel::lastMessageAt)
    val request = QueryChannelsRequest(filter, offset, limit, sort)
    request.watch = true
    request.state = true
    client.queryChannels(request).enqueue {
        val channels = it.data()
    }
}

fun filter1() {
    val filter = Filters
        .`in`("members", "thierry")
        .put("type", "messaging")
}

fun filter2() {
    val filter = Filters
        .`in`("status", "pending", "open", "new")
        .put("agent_id", userId)
}

fun queryChannelsPaginating() {
    val filter = Filters.`in`("members", "thierry")
    val offset = 0
    val limit = 10
    val request = QueryChannelsRequest(filter, offset, limit)

    client.queryChannels(request).enqueue {
        val channels = it.data()
    }
}

fun updateChannel() {
    val updateMessage = Message()
    updateMessage.text = "Thierry changed the channel color to green"
    channelController.update(updateMessage).enqueue {
        val channel = it.data()
    }
}

fun addMembersAndRemoveMembers() {
    channelController.addMembers("thierry", "josh").enqueue {
        val channel = it.data()
    }
    channelController.removeMembers("thierry", "josh").enqueue {
        val channel = it.data()
    }
}

fun createChannel() {
    val members = listOf("thierry", "tommaso")
    client.createChannel("messaging", members).enqueue {
        val channel = it.data()
    }
}

fun inviteMembers() {
    val members = listOf("thierry", "tommaso")
    val invites = listOf("nick")
    val data = mutableMapOf<String, Any>()

    data["members"] = members
    data["invites"] = invites

    client.createChannel(channelType, channelId, data).enqueue {
        val channel = it.data()
    }
}

fun acceptInvite() {
    channelController.acceptInvite("Nick joined this channel!").enqueue {
        val channel = it.data()
    }
}

fun rejectInvite() {
    channelController.rejectInvite().enqueue {
        val channel = it.data()
    }
}

fun queryInvited() {
    val offset = 0
    val limit = 10
    val request = QueryChannelsRequest(FilterObject("invite", "accepted"), offset, limit)
    client.queryChannels(request).enqueue {
        val channels = it.data()
    }
}

fun queryRejected() {
    val offset = 0
    val limit = 10
    val request = QueryChannelsRequest(FilterObject("invite", "rejected"), offset, limit)
    client.queryChannels(request).enqueue {
        val channels = it.data()
    }
}

fun delete() {
    channelController.delete().enqueue {
        val channel = it.data()
    }
}

fun hide() {

    // hides the channel until a new message is added there
    channelController.hide().enqueue {
        val channel = it.data()
    }

    // shows a previously hidden channel
    channelController.show().enqueue {
        val channel = it.data()
    }

    // hide the channel and clear the message history
    channelController.hide(true).enqueue {
        val channel = it.data()
    }
}

fun muting() {
    client.muteChannel(channelType, channelId)
        .enqueue { result: Result<Unit> ->
            if (result.isSuccess) {
                // channel is muted
            } else {
                result.error().cause?.printStackTrace()
            }
        }

    // get list of muted channels when user is connected
    client.setUser(
        user,
        token,
        object : InitConnectionListener() {
            override fun onSuccess(data: ConnectionData) {
                val user = data.user
                // mutes contains the list of channel mutes
                val mutes: List<ChannelMute> = user.channelMutes
            }
        }
    )

    // get updates about muted channels
    client.subscribe { event: ChatEvent ->
        if (event is NotificationChannelMutesUpdatedEvent) {
            val mutes = event.me.channelMutes
        } else if (event is NotificationMutesUpdatedEvent) {
            val mutes = event.me.channelMutes
        }
    }
}

fun queryMuted() {
    // retrieve channels excluding muted ones
    val offset = 0
    val limit = 10
    val messageLimit = 0
    val sort = QuerySort<Channel>()

    val mutedFiler = Filters.eq("muted", false)

    client.queryChannels(
        QueryChannelsRequest(mutedFiler, offset, limit, sort, messageLimit)
    )
        .enqueue { result ->
            if (result.isSuccess) {
                val channels = result.data()
            } else {
                result.error().cause?.printStackTrace()
            }
        }

    // retrieve muted channels
    val unmutedFilter = Filters.eq("muted", true)

    client.queryChannels(
        QueryChannelsRequest(unmutedFilter, offset, limit, sort, messageLimit)
    )
        .enqueue { result ->
            if (result.isSuccess) {
                val channels = result.data()
            } else {
                result.error().cause?.printStackTrace()
            }
        }
}

fun unmute() {
    // unmute channel for current user
    channelController.unmute().enqueue { result ->
        if (result.isSuccess) {
            // channel is unmuted
        } else {
            result.error().cause?.printStackTrace()
        }
    }
}

fun setInvisbleUser() {
    val user = User(userId)
    user.invisible = true
    client.setUser(user, "{{ chat_user_token }}")
}

fun queryUsers() {

    // If you pass presence: true to channel.watch it will watch the list of user presence changes.
    // Note that you can listen to at most 10 users using this API call

    val watchRequest = WatchChannelRequest()
    watchRequest.data["members"] = listOf("john", "jack")

    channelController.watch(watchRequest).enqueue {
        val channel = it.data()
    }

    // queryChannels allows you to listen to the members of the channels that are returned
    // so this does the same thing as above and listens to online status changes for john and jack

    val wathRequestWithPresence = WatchChannelRequest()
    wathRequestWithPresence.presence = true
    wathRequestWithPresence.data["members"] = listOf("john", "jack")

    channelController.watch(wathRequestWithPresence).enqueue {
        val channel = it.data()
    }

    // queryUsers allows you to listen to user presence changes for john and jack

    val offset = 0
    val limit = 2
    val usersFilter = Filters.`in`("id", listOf("john", "jack"))
    val usersQuery = QueryUsersRequest(usersFilter, offset, limit)
    usersQuery.presence = true
    client.queryUsers(usersQuery).enqueue {
        val users = it.data()
    }
}

fun startAndStopTyping() {
    // sends a typing.start event at most once every two seconds
    channelController.keystroke()
    // sends the typing.stop event
    channelController.stopTyping()
}

fun reveivingTypingEvents() {
    // add typing start event handling
    channelController.subscribeFor(EventType.TYPING_START) { startTyping ->
    }
    // add typing top event handling
    channelController.subscribeFor(EventType.TYPING_STOP) { startTyping ->
    }
}

fun unreadSetUser() {
    client.setUser(
        User(userId),
        "{{ chat_user_token }}",
        object : InitConnectionListener() {
            override fun onSuccess(data: ConnectionData) {
                val user = data.user
                val unreadChannels = user.unreadChannels
                val totalUnreadCount = user.totalUnreadCount
            }
        }
    )
}

fun markRead() {
    channelController.markRead().enqueue {
        val readEvent = it.data()
    }
}

fun updateUsers() {
    val user = User(userId)
    client.updateUser(user).enqueue {
        val user = it.data()
    }
}

internal class ChannelPagination {

    init {
        loadFirstPage()
    }

    companion object {

        var pageSize = 10

        fun loadFirstPage() {
            val firstPage = QueryChannelRequest().withMessages(pageSize)
            client.queryChannel(channelType, channelId, firstPage).enqueue { result ->
                val messages: List<Message> = result.data().messages
                if (messages.isNotEmpty() && messages.size == pageSize) {
                    loadSecondPage(messages.last().id)
                } else {
                    // all messages loaded
                }
            }
        }

        fun loadSecondPage(lastMessageId: String) {
            val firstPage =
                QueryChannelRequest().withMessages(Pagination.LESS_THAN, lastMessageId, pageSize)
            client.queryChannel(channelType, channelId, firstPage).enqueue { result ->
                val messages: List<Message> = result.data().messages
                if (messages.size < pageSize) {
                    // all messages loaded
                } else {
                    // load another page
                }
            }
        }
    }
}

class MultiTenantAndTeams {

    fun channelTeam() {
        val extraData = mutableMapOf<String, Any>()
        extraData["team"] = "red"
        client.createChannel("messaging", "red-general", extraData)
            .enqueue { result ->
                if (result.isSuccess) {
                    val channel = result.data()
                } else {
                    val error = result.error()
                }
            }
    }

    fun userSearch() {
        val filter = and(
            Filters.eq("name", "Jordan"),
            Filters.eq("teams", contains("red"))
        )
        val offset = 0
        val limit = 1
        client.queryUsers(QueryUsersRequest(filter, offset, limit))
            .enqueue { result ->
                if (result.isSuccess) {
                    val users = result.data()
                } else {
                    val error = result.error()
                }
            }
    }
}

internal object Translation {
    fun translate() {
        val channelController = client.channel("messaging:general")
        val message =
            Message(text = "Hello, I would like to have more information about your product.")
        val frenchLanguage = "fr"
        channelController.sendMessage(message).enqueue { result ->
            val messageId = result.data().id
            client.translate(messageId, frenchLanguage).enqueue { result ->
                val translatedMessage = result.data()
                val translation = translatedMessage.getTranslation(frenchLanguage)
            }
        }
    }
}
