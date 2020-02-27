package io.getstream.chat.android.client.sample.common

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import io.getstream.chat.android.client.api.models.ChannelWatchRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ErrorEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters.eq
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.sample.App
import io.getstream.chat.android.client.sample.R
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.observable.Subscription
import kotlinx.android.synthetic.main.activity_test_api.*

class TestChannelsApiMethodsActivity : AppCompatActivity() {

    val client = App.client
    val channelId = "general"
    val channelType = "team"
    var chatSub: Subscription? = null
    var watchingChannel: Channel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_api)

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

        client.setUser(User("bender"))
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
                val watchResult = channel.watch(ChannelWatchRequest().withMessages(100)).execute()
                echoResult(watchResult)
            }
        }.start()

    }

    private fun markReadMessage() {
        client.markRead(channelType, channelId, "zed").enqueue {
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
        client.queryChannels(
            QueryChannelsRequest(
                eq("type", "messaging"),
                0,
                1
            )
        ).enqueue {
            echoResult(it)
        }
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