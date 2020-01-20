package io.getstream.chat.android.core.poc.app.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.core.poc.R
import io.getstream.chat.android.core.poc.app.App
import io.getstream.chat.android.core.poc.library.*
import io.getstream.chat.android.core.poc.library.requests.QuerySort
import kotlinx.android.synthetic.main.activity_test_api.*

class TestApiMethodsActivity : AppCompatActivity() {

    val client = App.client

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_api)



        btnQueryChannels.isEnabled = false
        btnUpdateChannel.isEnabled = false

        client.setUser(User("bender"), object : TokenProvider {
            override fun getToken(listener: TokenProvider.TokenProviderListener) {
                listener.onSuccess("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYmVuZGVyIn0.3KYJIoYvSPgTURznP8nWvsA2Yj2-vLqrm-ubqAeOlcQ")
            }
        }).enqueue {
            if (it.isSuccess) {
                initButtons()
            }
        }


    }

    private fun initButtons() {

        btnQueryChannels.isEnabled = true
        btnUpdateChannel.isEnabled = true

        btnQueryChannels.setOnClickListener { queryChannels() }
        btnUpdateChannel.setOnClickListener { updateChannel() }
    }

    private fun updateChannel() {
        val channelId = "demo"
        val channelType = "messaging"
        val message = Message()
        message.text = "Hello"
        client.updateChannel(channelId, channelType, message).enqueue {
            if (it.isSuccess) {

            }
        }
    }

    private fun queryChannels() {
        client.queryChannels(
            QueryChannelsRequest(
                FilterObject(),
                QuerySort()
            ).withLimit(1)
        ).enqueue {
            if (it.isSuccess) {

            } else {

            }
        }
    }
}