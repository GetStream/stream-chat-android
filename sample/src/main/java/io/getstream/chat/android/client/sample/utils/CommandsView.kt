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
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.models.*
import io.getstream.chat.android.client.notifications.options.ChatNotificationConfig
import io.getstream.chat.android.client.sample.App
import io.getstream.chat.android.client.sample.R
import io.getstream.chat.android.client.token.TokenProvider
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.observable.Subscription
import kotlinx.android.synthetic.main.layout_commands.view.*
import java.util.*
import java.util.concurrent.TimeUnit

class CommandsView(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    init {
        orientation = LinearLayout.VERTICAL
        LayoutInflater.from(context).inflate(R.layout.layout_commands, this, true)
    }

    private val subs = mutableListOf<Subscription>()
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

        val notificationsConfig = object : ChatNotificationConfig(context) {
            override fun onFirebaseMessage(message: RemoteMessage): Boolean {
                return true
            }
        }

        if (customUrl.isNullOrEmpty()) {
            if (useStaging) {
                client = ChatClient.Builder(config.apiKey, App.instance)
                    .baseUrl(stagingEndpoint)
                    .notifications(notificationsConfig)
                    .build()
            } else {
                client = ChatClient.Builder(config.apiKey, App.instance)
                    .notifications(notificationsConfig)
                    .build()
            }
        } else {
            client = ChatClient.Builder(config.apiKey, App.instance)
                .baseUrl(customUrl)
                .notifications(notificationsConfig)
                .build()
        }


        subs.add(client.events()
            .filter(ConnectedEvent::class.java)
            .filter(DisconnectedEvent::class.java)
            .filter(ConnectingEvent::class.java)
            .subscribe { event ->
                textStatus.text = event.type
                Log.d("connection-events", event::class.java.simpleName)
            })

        textUserId.text = "UserId: ${config.userId}"

        btnConnect.setOnClickListener {

            client.setUser(config.getUser(), object : TokenProvider {
                override fun loadToken(): String {
                    Thread.sleep(1000)
                    return config.token
                }
            })
        }

        btnDisconnect.setOnClickListener {
            client.disconnect()
        }

        btnAddDevice.setOnClickListener {
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {

                val token = it.token

                client.addDevice(token).enqueue { addDeviceResult ->
                    UtilsMessages.show("device added", "device not added: ", addDeviceResult)
                }
            }
        }

        btnRemoveDevice.setOnClickListener {

            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {

                val token = it.token
                client.deleteDevice(token).enqueue { deleteDeviceResult ->
                    UtilsMessages.show("removed", "not removed: ", deleteDeviceResult)
                }
            }


        }

        btnStartWatchingChannel.setOnClickListener {


            client.queryChannel(chType, chId, request).enqueue { watchResult ->
                UtilsMessages.show("started", "not not started:", watchResult)
            }
        }

        btnStopWatchingChannel.setOnClickListener {

            client.stopWatching(chType, chId).enqueue { stopWatchResult ->
                UtilsMessages.show("stopped", "not stopped:", stopWatchResult)
            }
        }

        btnUpdateChannel.setOnClickListener {
            val data = mutableMapOf<String, Any>()
            data["name"] = chId
            client.updateChannel(chType, chId, Message("update-msg"), data).enqueue {
                UtilsMessages.show("updated", "not updated:", it)
            }
        }

        btnSendMessage.setOnClickListener {

            val messageOut = Message(text = "from llc sample at ${System.currentTimeMillis()}")
            messageOut.extraData["test"] = "zed"
            client.sendMessage(chType, chId, messageOut).enqueue { messageResult ->
                if (messageResult.isSuccess) {
                    val messageIn = messageResult.data()
                }

                UtilsMessages.show("sent", "not sent:", messageResult)

            }
        }

        btnGetMessages.setOnClickListener {
            val queryChannelRequest = QueryChannelRequest().withMessages(100)

            client.queryChannel(chType, chId, queryChannelRequest).enqueue {
                UtilsMessages.show(it)
            }
        }

        btnMarkAllRead.setOnClickListener {
            client.markAllRead().enqueue {
                UtilsMessages.show(it)
            }
        }

        btnMarkChannelRead.setOnClickListener {
            client.markAllRead().enqueue {
                UtilsMessages.show(it)
            }
        }

        btnQueryChannel.setOnClickListener {

            val request = QueryChannelRequest()
                .withMessages(100)

            //request.messages["attachments"] = "{\$exists:true}"
            //request.messages["text"] = Filters.eq("text", "SSS")
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

        btnTranslateMessage.setOnClickListener {

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

        btnQueryUsers.setOnClickListener {

            val filter = Filters.eq("id", config.userId)

            client.queryUsers(QueryUsersRequest(filter, 0, 10)).enqueue {
                UtilsMessages.show(it)
            }
        }

        btnQueryMembers.setOnClickListener {
            client.queryMembers(chType, chId, 0, 10, Filters.eq("invited", true)).enqueue {
                UtilsMessages.show(it)
            }
        }



        btnGetOrCreateChannel.setOnClickListener {

            val queryChannelRequest = QueryChannelRequest()
                .withData(mapOf("name" to chId))
                .withMessages(5)

            client.queryChannel(chType, chId, queryChannelRequest).enqueue {

                if (it.isError) {
                    it.error().printStackTrace()
                }

                UtilsMessages.show("query success", "query error", it)
            }


        }



        btnGet5MinSyncHistory.setOnClickListener {


            //val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            //val lastSyncAt = format.parse("2020-06-16T11:07:05.699Z")
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

        btnGetAllSyncHistory.setOnClickListener {

            client.getSyncHistory(listOf("$chType:$chId"), Date(0)).enqueue {
                UtilsMessages.show("History received", "History not received", it)
            }

        }

        btnSearchMessage.setOnClickListener {

            val channelFiler = Filters.and(
                Filters.eq("type", chType),
                Filters.eq("id", chId)
            )

            val messageFilter = Filters.and(
                Filters.eq("text", ""),
                Filters.eq("attachments", Filters.eq("\$exists", true))
            )

//            val messageFilter = Filters.eq("attachments", Filters.eq("\$exists", true))

            client.searchMessages(
                SearchMessagesRequest(0, 100, channelFiler, messageFilter)
            ).enqueue {
                UtilsMessages.show(it)
            }
        }

    }

    fun destroy() {
        subs.forEach { it.unsubscribe() }
        client.disconnect()
    }
}