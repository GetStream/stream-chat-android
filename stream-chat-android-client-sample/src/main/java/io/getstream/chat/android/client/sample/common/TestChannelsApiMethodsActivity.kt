package io.getstream.chat.android.client.sample.common

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ErrorEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters.`in`
import io.getstream.chat.android.client.models.Filters.and
import io.getstream.chat.android.client.models.Filters.eq
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.sample.App
import io.getstream.chat.android.client.sample.databinding.ActivityTestApiBinding
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.android.client.socket.SocketListener
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.observable.Disposable

class TestChannelsApiMethodsActivity : AppCompatActivity() {

    val client = App.client
    val channelId = "general"
    val channelType = "team"

    var chatDisposable: Disposable? = null
    var watchingChannel: Channel? = null

    val benderUserId = "bender"

    val benderToken =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYmVuZGVyIn0.3KYJIoYvSPgTURznP8nWvsA2Yj2-vLqrm-ubqAeOlcQ"

    val benderZUserId = "bender-z"

    val benderZToken =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiYmVuZGVyLXoifQ.ZGXziY6D_Stv57n3elJrLi-3DulawwSXw-IZk_w2zoI"

    val benderXUserId = "bender-x"

    val benderXToken =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiYmVuZGVyLXgifQ.LDgXkymWSYSq0GD6oosc0PNpUytR8Md9m1bvJLl1QCY"

    val benderFUserId = "bender-f"

    val benderFToken =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiYmVuZGVyLWYifQ.7xHlQUI276vLSd_0r5TqqPxjEjwOYr6kelhODLRgUs4\n"

    private lateinit var binding: ActivityTestApiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestApiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val ctx = this

        binding.buttonsContainer.children.iterator().forEach {
            it.isEnabled = false
        }

        chatDisposable = client.subscribe {
            if (it is ConnectedEvent) {
                initButtons()
            } else if (it is ErrorEvent) {
                Toast.makeText(this, "Error chat connecting", Toast.LENGTH_SHORT).show()
            }
        }

        val user = User(benderUserId)
        user.extraData["name"] = benderUserId

        client.setUser(
            user,
            benderToken,
            object : InitConnectionListener() {
                override fun onSuccess(data: ConnectionData) {
                    val updatedUser = data.user
                    val connectionId = data.connectionId
                }

                override fun onError(error: ChatError) {
                    val message = error.message
                    Toast.makeText(ctx, "error setting user: $message", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    override fun onDestroy() {
        chatDisposable?.dispose()
        client.disconnect()
        super.onDestroy()
    }

    private fun initButtons() {
        binding.apply {
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
            btnCheckTyping.setOnClickListener { checkTyping() }
            btnUpdateMessage.setOnClickListener { updatedMessage() }
        }
    }

    private fun updatedMessage() {
        client.queryChannels(QueryChannelsRequest(FilterObject(), 0, 1).withMessages(1))
            .enqueue { queryResult ->

                if (queryResult.isSuccess) {
                    val channel = queryResult.data()[0]
                    val message = channel.messages[0]
                    message.text = message.text + "a"
                    client.updateMessage(message).enqueue { updateResult ->
                        if (updateResult.isSuccess) {
                        }
                    }
                }
            }
    }

    private fun checkTyping() {
        client.subscribeFor(TypingStartEvent::class.java) {
            println("checkTyping: received")
        }

        client.queryChannels(QueryChannelsRequest(FilterObject("type", "messaging"), 0, 1))
            .enqueue {
                val channel = it.data()[0]

                val controller = client.channel(channel.cid)

                controller.watch().enqueue {
                    controller.keystroke().enqueue {
                        if (it.isSuccess)
                            println("checkTyping: sent")
                        else
                            it.error().cause?.printStackTrace()
                    }
                }
            }
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

                val request = WatchChannelRequest().withMessages(100)
                val watchResult =
                    client.channel(channelType, channelId).watch(request).execute()
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

        val filter = and(eq("type", "messaging"), `in`("members", benderUserId))

        client.queryChannels(
            QueryChannelsRequest(filter, 0, 1)
        ).enqueue {

            if (it.isSuccess) {
                val channels = it.data()
                val channel = channels[0]
                val type = channel.type
                val id = channel.id

                val controller = client.channel(type, id)
                controller.watch().enqueue {
                    if (it.isSuccess) {
                    }
                }
            }

            echoResult(it)
        }
    }

    private fun getMessage() {
        Thread {

            val request =
                QueryChannelsRequest(FilterObject("type", "messaging"), 0, 1).withMessages(1)

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
        client.addSocketListener(
            object : SocketListener() {
// override required methods
            }
        )
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
                result.error().cause?.printStackTrace()

                val message = result.error().message
                Toast.makeText(this, "$error: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
