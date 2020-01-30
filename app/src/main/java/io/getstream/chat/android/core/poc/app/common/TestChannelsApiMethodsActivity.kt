package io.getstream.chat.android.core.poc.app.common

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import io.getstream.chat.android.core.poc.R
import io.getstream.chat.android.core.poc.app.App
import io.getstream.chat.android.core.poc.library.*
import io.getstream.chat.android.core.poc.library.events.ConnectionEvent
import io.getstream.chat.android.core.poc.library.rest.ChannelWatchRequest
import io.getstream.chat.android.core.poc.library.socket.ChatObservable
import kotlinx.android.synthetic.main.activity_test_api.*

class TestChannelsApiMethodsActivity : AppCompatActivity() {

    val client = App.client
    val channelId = "general"
    val channelType = "team"
    var chatSub: ChatObservable.Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_api)

        buttonsContainer.children.iterator().forEach {
            it.isEnabled = false
        }

        client.setUser(User("stream-eugene"), object : TokenProvider {
            override fun getToken(listener: TokenProvider.TokenProviderListener) {
                listener.onSuccess("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoic3RyZWFtLWV1Z2VuZSJ9.-WNauu6xV56sHM39ZrhxDeBiKjA972O5AYo-dVXva6I")
            }
        }).subscribe {

            if (it is ConnectionEvent) {
                //echoResult(it, "Connected", "Socket connection error")
                initButtons()
            }
        }
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


        chatSub = client.events().subscribe {
            Log.d("events", it.toString())
        }
    }

    private fun watchChannel() {

        Thread {

            val withLimit = QueryChannelsRequest(
                0,
                100
            )

            val channelsResult = client.queryChannels(withLimit).execute()

            echoResult(channelsResult)

            if (channelsResult.isSuccess) {
                val watchResult = channelsResult.data()[0].watch(ChannelWatchRequest()).execute()

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
                0,
                100
            )
        ).enqueue {
            echoResult(it)
        }
    }

    private fun stopWatching() {

        client.stopWatching("messaging", "new-ch").enqueue {
            echoResult(it)
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
                Toast.makeText(this, "$error: $message", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}