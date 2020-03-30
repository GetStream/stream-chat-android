package io.getstream.chat.android.client.sample.common

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import io.getstream.chat.android.client.api.models.ChannelWatchRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ErrorEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters.`in`
import io.getstream.chat.android.client.models.Filters.and
import io.getstream.chat.android.client.models.Filters.eq
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.sample.App
import io.getstream.chat.android.client.sample.R
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.android.client.socket.SocketListener
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.observable.Subscription
import kotlinx.android.synthetic.main.activity_test_api.*

class TestChannelsApiMethodsActivity : AppCompatActivity() {

    val client = App.client
    val channelId = "general"
    val channelType = "team"
    val userId = "bender"
    var chatSub: Subscription? = null
    var watchingChannel: Channel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_api)

        val ctx = this

        buttonsContainer.children.iterator().forEach {
            it.isEnabled = false
        }

        chatSub = client.events().subscribe {

            if (it is ConnectedEvent) {
                initButtons()
            } else if (it is ErrorEvent) {
                Toast.makeText(this, "Error chat connecting", Toast.LENGTH_SHORT).show()
            }
        }

        val token =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYmVuZGVyIn0.3KYJIoYvSPgTURznP8nWvsA2Yj2-vLqrm-ubqAeOlcQ"

        client.setUser(User(userId), token, object : InitConnectionListener() {
            override fun onSuccess(data: ConnectionData) {
                val user = data.user
                val connectionId = data.connectionId
            }

            override fun onError(error: ChatError) {
                val message = error.message
                Toast.makeText(ctx, "error setting user: $message", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onDestroy() {
        chatSub?.unsubscribe()
        client.disconnect()
        super.onDestroy()
    }

    private fun initButtons() {

        buttonsContainer.children.iterator().forEach {
            it.isEnabled = true
        }

        btnQueryChannels.setOnClickListener { queryChannels() }
        btnUpdateChannel.setOnClickListener { updateChannel() }
        btnStopWatching.setOnClickListener { stopWatching() }
        btnAcceptInvite.setOnClickListener { acceptInvite() }
        btnRejectInvite.setOnClickListener { rejectInvite() }
        btnHideChannel.setOnClickListener { hideChannel() }
        btnShowChannel.setOnClickListener { showChannel() }
        btnMarkReadMessage.setOnClickListener { markReadMessage() }
        btnWatchChannel.setOnClickListener { watchChannel() }
        btnGetMessage.setOnClickListener { getMessage() }
    }

    private fun watchChannel() {

        Thread {

            val withLimit =
                QueryChannelsRequest(FilterObject("type", "messaging"), 0, 100).withMessages(100)

            val channelsResult = client.queryChannels(withLimit).execute()

            echoResult(channelsResult)

            if (channelsResult.isSuccess) {
                val channels = channelsResult.data()
                val channel = channels[0]

                val watchResult =
                    client.channel(channelType, channelId).watch((ChannelWatchRequest().withMessages(100))).execute()
                echoResult(watchResult)
            }
        }.start()

    }

    private fun markReadMessage() {
        client.markMessageRead(channelType, channelId, "zed").enqueue {
            echoResult(it)
        }
    }

    private fun showChannel() {
        client.showChannel(channelType, channelId).enqueue {
            echoResult(it)
        }
    }

    private fun hideChannel() {
        client.hideChannel(channelType, channelId).enqueue {
            echoResult(it)
        }
    }

    private fun rejectInvite() {
        client.rejectInvite(channelType, channelId).enqueue {
            echoResult(it)
        }
    }

    private fun acceptInvite() {
        client.acceptInvite(channelType, channelId, "hello-accept").enqueue {
            echoResult(it)
        }
    }

    private fun updateChannel() {
        val message = Message()
        message.text = "Hello"
        client.updateChannel(channelType, channelId, message).enqueue {
            echoResult(it)
        }
    }

    private fun queryChannels() {

        val filter = and(eq("type", "messaging"), `in`("members", userId))

        client.queryChannels(
            QueryChannelsRequest(filter, 0, 1)
        ).enqueue {

            val channels = it.data()
            val channel = channels[0]
            val type = channel.type
            val id = channel.id

            val controller = client.channel(type, id)
            controller.watch().enqueue {
                if (it.isSuccess) {

                }
            }

            echoResult(it)
        }
    }

    private fun getMessage() {
        Thread {

            val request = QueryChannelsRequest(FilterObject("type", "messaging"), 0, 1).withMessages(1)

            val channelsResult = client.queryChannels(request).execute()

            if (channelsResult.isSuccess) {

                val channels = channelsResult.data()
                val channel = channels[0]
                val message = channel.messages[0]
                val messageResult = client.getMessage(message.id).execute()

                if (messageResult.isSuccess) {
                    val returnedMessage = messageResult.data()

                    if (returnedMessage.id == message.id) {

                    }
                }
            }
        }.start()
    }

    fun getChannels() {
        client.addSocketListener(object : SocketListener() {
            //override required methods

        })
    }

    private fun stopWatching() {
        val channel = watchingChannel
        if (channel != null) {
            client.stopWatching(channel.type, channel.id).enqueue {
                echoResult(it)
            }
        }
    }

    private fun echoResult(
        result: Result<*>,
        success: String = "Success",
        error: String = "Error"
    ) {
        runOnUiThread {
            if (result.isSuccess) {
                Toast.makeText(this, success, Toast.LENGTH_SHORT).show()
            } else {
                result.error().printStackTrace()

                val message = result.error().message
                Toast.makeText(this, "$error: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }
}