package io.getstream.chat.android.core.poc.app.common

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.core.poc.R
import io.getstream.chat.android.core.poc.library.ChatClientBuilder
import io.getstream.chat.android.core.poc.library.TokenProvider
import io.getstream.chat.android.core.poc.library.User
import io.getstream.chat.android.core.poc.library.api.ApiClientOptions
import io.getstream.chat.android.core.poc.library.events.ConnectionEvent
import kotlinx.android.synthetic.main.activity_socket_tests.*

class SocketTestActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_socket_tests)

        val apiOptions = ApiClientOptions.Builder()
            .baseURL("chat-us-east-1.stream-io-api.com")
            .cdnUrl("chat-us-east-1.stream-io-api.com")
            .timeout(10000)
            .cdnTimeout(10000)
            .build()
        val apiKey = "qk4nn7rpcn75"
        val client = ChatClientBuilder(apiKey, apiOptions).build()

        client.events().subscribe {

        }

        btnConnect.setOnClickListener {

            textSocketState.text = "Connecting..."

            client.setUser(User("bender"), object : TokenProvider {
                override fun getToken(listener: TokenProvider.TokenProviderListener) {
                    listener.onSuccess("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYmVuZGVyIn0.3KYJIoYvSPgTURznP8nWvsA2Yj2-vLqrm-ubqAeOlcQ")
                }
            }).subscribe {

                Log.d("evt", it.type)

                textSocketEvent.text = it.type + " at " + it.createdAt

                if (it is ConnectionEvent) {
                    Toast.makeText(this, "Connection resolved", Toast.LENGTH_LONG).show()
                }

//                if (it.isSuccess) {
//                    textSocketState.text = "Connected with " + it.data().user.id
//                } else {
//                    textSocketState.text = "Connection error " + it.error()
//                }
            }
        }

        btnDisconnect.setOnClickListener {
            client.disconnect()
            textSocketState.text = "Disconnected"
        }


    }
}