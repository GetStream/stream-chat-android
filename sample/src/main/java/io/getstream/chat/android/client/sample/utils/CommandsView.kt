package io.getstream.chat.android.client.sample.utils

import android.content.Context
import android.util.AttributeSet
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
import io.getstream.chat.android.client.notifications.options.ChatNotificationConfig
import io.getstream.chat.android.client.sample.App
import io.getstream.chat.android.client.sample.R
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
    val request = QueryChannelRequest().withWatch()

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

        client = ChatClient.Builder(config.apiKey, App.instance)
            .notifications(object : ChatNotificationConfig(context) {
                override fun onFirebaseMessage(message: RemoteMessage): Boolean {

                    textPush.text = "Received: ${message.hashCode()}"

                    return super.onFirebaseMessage(message)
                }
            })
            .build()

        subs.add(client.events()
            .filter(ConnectedEvent::class.java)
            .filter(DisconnectedEvent::class.java)
            .filter(ConnectingEvent::class.java)
            .subscribe { event ->
                textStatus.text = event.type

                if(event is ConnectingEvent){
                    //
                }
            })

        textUserId.text = "UserId: ${config.userId}"

        btnConnect.setOnClickListener {
            client.setUser(config.getUser(), config.token)
        }

        btnDisconnect.setOnClickListener {
            client.disconnect()
        }

        btnAddDevice.setOnClickListener {
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {

                val token = it.token

                client.addDevice(token).enqueue { addDeviceResult ->
                    if (addDeviceResult.isSuccess) {

                    }
                }
            }
        }

        btnRemoveDevice.setOnClickListener {

            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {

                val token = it.token
                client.deleteDevice(token).enqueue { result ->
                    if (result.isSuccess) {

                    }
                }
            }


        }

        btnStartWatchingChannel.setOnClickListener {


            client.queryChannel(chType, chId, request).enqueue { watchResult ->
                if (watchResult.isSuccess) {

                }
            }
        }

        btnStopWatchingChannel.setOnClickListener {

            client.stopWatching(chType, chId).enqueue { stopWatchResult ->
                if (stopWatchResult.isSuccess) {

                }
            }
        }

        btnSendMessage.setOnClickListener {
            client.sendMessage(chType, chId, Message(text = "SSS")).enqueue { messageResult ->
                if (messageResult.isSuccess) {

                }
            }
        }
    }

    fun destroy() {
        subs.forEach { it.unsubscribe() }
        client.disconnect()
    }
}