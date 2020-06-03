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
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.getUnreadMessagesCount
import io.getstream.chat.android.client.notifications.options.ChatNotificationConfig
import io.getstream.chat.android.client.sample.App
import io.getstream.chat.android.client.sample.R
import io.getstream.chat.android.client.token.TokenProvider
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.observable.Subscription
import kotlinx.android.synthetic.main.layout_commands.view.*

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

    val chType = "messaging"
    val chId = "x-test"
    lateinit var members: List<String>
    lateinit var config: UserConfig

    fun setUser(config: UserConfig, members: List<String>) {

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
        client = ChatClient.Builder(config.apiKey, App.instance)
            .notifications(notificationsConfig)
            .build()

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
            val messageOut = Message(text = "SSS")
            messageOut.extraData["test"] = "zed"
            client.sendMessage(chType, chId, messageOut).enqueue { messageResult ->
                if (messageResult.isSuccess) {
                    val messageIn = messageResult.data()
                }

                UtilsMessages.show("sent", "not sent:", messageResult)

            }
        }

        btnGetMessages.setOnClickListener {
            val queryChannelRequest = QueryChannelRequest().withMessages(5)

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

    }

    fun destroy() {
        subs.forEach { it.unsubscribe() }
        client.disconnect()
    }
}