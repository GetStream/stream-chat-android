package io.getstream.chat.android.core.poc.app.common

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.core.poc.R
import io.getstream.chat.android.core.poc.app.App
import io.getstream.chat.android.core.poc.library.TokenProvider
import io.getstream.chat.android.core.poc.library.User
import kotlinx.android.synthetic.main.activity_socket_tests.*

class SocketTestActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_socket_tests)

        textSocketState.text = "Connecting..."

        App.client.setUser(User("bender"), object : TokenProvider {
            override fun getToken(listener: TokenProvider.TokenProviderListener) {
                listener.onSuccess("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYmVuZGVyIn0.3KYJIoYvSPgTURznP8nWvsA2Yj2-vLqrm-ubqAeOlcQ")
            }
        }) { connection, error ->
            if (error == null) {
                textSocketState.text = "Connected with " + connection.user.id
            } else {
                textSocketState.text = "Connection error " + error
            }
        }
    }
}