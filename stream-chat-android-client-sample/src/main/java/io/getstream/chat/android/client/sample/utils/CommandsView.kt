package io.getstream.chat.android.client.sample.utils

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.api.models.SearchMessagesRequest
import io.getstream.chat.android.client.events.ChannelUserBannedEvent
import io.getstream.chat.android.client.events.ChannelUserUnbannedEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.GlobalUserBannedEvent
import io.getstream.chat.android.client.events.GlobalUserUnbannedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.getTranslation
import io.getstream.chat.android.client.models.getUnreadMessagesCount
import io.getstream.chat.android.client.models.originalLanguage
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.sample.App
import io.getstream.chat.android.client.sample.databinding.LayoutCommandsBinding
import io.getstream.chat.android.client.subscribeFor
import io.getstream.chat.android.client.token.TokenProvider
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.observable.Disposable
import java.util.Date
import java.util.concurrent.TimeUnit

class CommandsView(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    val binding = LayoutCommandsBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = VERTICAL
    }

    private val disposables = mutableListOf<Disposable>()
    lateinit var client: ChatClient

    val filter = FilterObject("type", "messaging")
    val sort = QuerySort().asc("created_at")
    val request = QueryChannelRequest().withWatch().withMessages(10)
    val stagingEndpoint = "chat-us-east-staging.stream-io-api.com"

    val chType = "messaging"
    val chId = "x-test"
    val cid = "$chType:$chId"
    lateinit var members: List<String>
    lateinit var config: UserConfig

    fun setUser(
        config: UserConfig,
        members: List<String>,
        useStaging: Boolean = false,
        customUrl: String = ""
    ) {

        this.config = config
        this.members = members

        val data = mutableMapOf<String, Any>()
        data["members"] = members

        request.withData(data)

        val notificationsHandler = object : ChatNotificationHandler(context) {
            override fun onFirebaseMessage(message: RemoteMessage): Boolean {
                return true
            }
        }

        client = if (customUrl.isEmpty()) {
            if (useStaging) {
                ChatClient.Builder(config.apiKey, App.instance)
                    .baseUrl(stagingEndpoint)
                    .notifications(notificationsHandler)
                    .build()
            } else {
                ChatClient.Builder(config.apiKey, App.instance)
                    .notifications(notificationsHandler)
                    .build()
            }
        } else {
            ChatClient.Builder(config.apiKey, App.instance)
                .baseUrl(customUrl)
                .notifications(notificationsHandler)
                .build()
        }

        disposables.add(
            client.subscribeFor(ConnectedEvent::class) {
                println(it)
            }
        )

        disposables.add(
            client.subscribeFor(
                ConnectedEvent::class,
                NotificationChannelMutesUpdatedEvent::class
            ) {
                var mutedChannels: List<ChannelMute> = emptyList()
                if (it is ConnectedEvent) {
                    mutedChannels = it.me.channelMutes
                } else if (it is NotificationChannelMutesUpdatedEvent) {
                    mutedChannels = it.me.channelMutes
                }
                println(mutedChannels)
            }
        )

        disposables.add(
            client.subscribeFor(
                ConnectedEvent::class,
                ConnectingEvent::class,
                DisconnectedEvent::class
            ) { event ->
                binding.textStatus.text = event.type
                Log.d("connection-events", event::class.java.simpleName)
            }
        )

        disposables.add(
            client.subscribeFor(
                ChannelUserUnbannedEvent::class,
                GlobalUserUnbannedEvent::class,
                ChannelUserBannedEvent::class,
                GlobalUserBannedEvent::class
            ) {
                println("ban/unban for " + config.userId + ": " + it.type)
            }
        )

        binding.textUserId.text = "UserId: ${config.userId}"

        binding.btnConnect.setOnClickListener {

            val user = config.getUser()
            user.extraData.clear()

            client.setUser(
                user,
                object : TokenProvider {
                    override fun loadToken(): String {
                        Thread.sleep(1000)
                        return config.token
                    }
                }
            )
        }

        binding.btnDisconnect.setOnClickListener {
            client.disconnect()
        }

        binding.btnAddDevice.setOnClickListener {
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {

                val token = it.token

                client.addDevice(token).enqueue { addDeviceResult ->
                    UtilsMessages.show("device added", "device not added: ", addDeviceResult)
                }
            }
        }

        binding.btnRemoveDevice.setOnClickListener {

            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {

                val token = it.token
                client.deleteDevice(token).enqueue { deleteDeviceResult ->
                    UtilsMessages.show("removed", "not removed: ", deleteDeviceResult)
                }
            }
        }

        binding.btnStartWatchingChannel.setOnClickListener {

            client.queryChannel(chType, chId, request).enqueue { watchResult ->
                UtilsMessages.show("started", "not not started:", watchResult)
            }
        }

        binding.btnStopWatchingChannel.setOnClickListener {

            client.stopWatching(chType, chId).enqueue { stopWatchResult ->
                UtilsMessages.show("stopped", "not stopped:", stopWatchResult)
            }
        }

        binding.btnUpdateChannel.setOnClickListener {
            val data = mutableMapOf<String, Any>()
            data["name"] = chId
            client.updateChannel(chType, chId, Message("update-msg"), data).enqueue {
                UtilsMessages.show("updated", "not updated:", it)
            }
        }

        binding.btnSendMessage.setOnClickListener {

            val messageOut = Message(text = "from llc sample at ${System.currentTimeMillis()}")
            messageOut.extraData["test"] = "zed"
            messageOut.mentionedUsersIds.add("stream-eugene")
            client.sendMessage(chType, chId, messageOut).enqueue { messageResult ->
                if (messageResult.isSuccess) {
                    val messageIn = messageResult.data()
                }

                UtilsMessages.show("sent", "not sent:", messageResult)
            }
        }

        binding.btnGetMessages.setOnClickListener {
            val queryChannelRequest = QueryChannelRequest().withMessages(1)

            client.queryChannel(chType, chId, queryChannelRequest).enqueue {
                UtilsMessages.show(it)
            }
        }

        binding.btnMarkAllRead.setOnClickListener {
            client.markAllRead().enqueue {
                UtilsMessages.show(it)
            }
        }

        binding.btnMarkChannelRead.setOnClickListener {
            client.markAllRead().enqueue {
                UtilsMessages.show(it)
            }
        }

        binding.btnQueryChannel.setOnClickListener {

            val request = QueryChannelRequest()
                .withMessages(100)

            // request.messages["attachments"] = "{\$exists:true}"
            // request.messages["text"] = Filters.eq("text", "SSS")
            request.messages["text"] = "SSS"

            client.queryChannel(chType, chId, request).enqueue {
                if (it.isSuccess) {
                    val channel = it.data()
                    val totalUnread = channel.getUnreadMessagesCount()
                    val unreadForCurrentUser = channel.getUnreadMessagesCount(config.userId)

                    println(totalUnread)
                    println(unreadForCurrentUser)
                }
                UtilsMessages.show(it)
            }
        }

        binding.btnTranslateMessage.setOnClickListener {

            val language = "nl"

            client.sendMessage(chType, chId, Message(text = "how are you?")).enqueue {
                if (it.isSuccess) {

                    client.translate(it.data().id, language).enqueue { result ->
                        val message = result.data()
                        val originalLanguage = message.originalLanguage
                        val translation = message.getTranslation(language)
                        println(originalLanguage)
                        println(translation)
                    }
                }
            }
        }

        binding.btnQueryUsers.setOnClickListener {

            val filter = Filters.eq("id", config.userId)

            client.queryUsers(QueryUsersRequest(filter, 0, 10)).enqueue {
                UtilsMessages.show(it)
            }
        }

        binding.btnQueryMembers.setOnClickListener {
            client.queryMembers(chType, chId, 0, 10, Filters.eq("banned", true)).enqueue {
                UtilsMessages.show(it)
            }
        }

        binding.btnGetOrCreateChannel.setOnClickListener {

            val queryChannelRequest = QueryChannelRequest()
                .withData(mapOf("name" to chId))
                .withMessages(5)

            client.queryChannel(chType, chId, queryChannelRequest).enqueue {

                if (it.isError) {
                    it.error().cause?.printStackTrace()
                }

                UtilsMessages.show("query success", "query error", it)
            }
        }

        binding.btnGet5MinSyncHistory.setOnClickListener {

            // val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            // val lastSyncAt = format.parse("2020-06-16T11:07:05.699Z")
//            val lastSyncAt = format.parse("2020-06-14T23:00:00Z")

            val now = System.currentTimeMillis()
            val ago5Min = TimeUnit.MINUTES.toMillis(5)
            val utcMs = now - ago5Min
            val lastSyncAt = Date(utcMs)

            client.getSyncHistory(
                listOf("$chType:$chId", "messaging:zed", "messaging:sss"),
                lastSyncAt
            ).enqueue {

                if (it.isSuccess && it.data().isNotEmpty()) {
                    val event = it.data().first()
                    if (event is NewMessageEvent) {
                        val message = event.message

                        if (message.createdAt != null) {
                            UtilsMessages.show(
                                "History received: last message at " + message.createdAt,
                                "History not received",
                                it
                            )
                        }
                    }
                } else {
                    UtilsMessages.show("History received", "History not received", it)
                }
            }
        }

        binding.btnGetAllSyncHistory.setOnClickListener {

            client.getSyncHistory(listOf("$chType:$chId"), Date(0)).enqueue {
                UtilsMessages.show("History received", "History not received", it)
            }
        }

        binding.btnSearchMessage.setOnClickListener {

            val channelFiler = Filters.eq("cid", cid)
            val messageFilter = Filters.eq("attachments", Filters.exists(true))

            client.searchMessages(
                SearchMessagesRequest(0, 100, channelFiler, messageFilter)
            ).enqueue {
                UtilsMessages.show(it)
            }
        }

        binding.btnUploadImage.setOnClickListener {
            // client.sendFile(chType, chId, File())
        }

        binding.btnBanUser.setOnClickListener {
            client.banUser(config.userId, chType, chId, "reason-z", 1).enqueue {
                UtilsMessages.show(it)
            }
        }

        binding.btnUnbanUser.setOnClickListener {
            client.unBanUser(config.userId, chType, chId).enqueue {
            }
        }

        binding.btnMuteUser.setOnClickListener {
            client.muteUser(config.userId).enqueue {
                UtilsMessages.show(it)
            }
        }

        binding.btnMuteChannel.setOnClickListener {
            client.muteChannel(chType, chId).enqueue {
                UtilsMessages.show(it)
            }
        }

        binding.btnUnMuteChannel.setOnClickListener {
            client.unMuteChannel(chType, chId).enqueue {
                UtilsMessages.show(it)
            }
        }
    }

    fun destroy() {
        disposables.forEach { it.dispose() }
        client.disconnect()
    }
}
