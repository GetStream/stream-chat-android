package io.getstream.chat.android.client.sample.common

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.ErrorEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.sample.R
import io.getstream.chat.android.client.utils.observable.Subscription
import kotlinx.android.synthetic.main.activity_socket_tests.btnConnect
import kotlinx.android.synthetic.main.activity_socket_tests.btnDisconnect
import kotlinx.android.synthetic.main.activity_socket_tests.textSocketEvent
import kotlinx.android.synthetic.main.activity_socket_tests.textSocketState
import java.text.SimpleDateFormat
import kotlin.time.ExperimentalTime

@ExperimentalTime
class SocketTestActivity : AppCompatActivity() {

    var subs = mutableListOf<Subscription>()

    val token =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYmVuZGVyIn0.3KYJIoYvSPgTURznP8nWvsA2Yj2-vLqrm-ubqAeOlcQ"

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_socket_tests)

        val client = ChatClient.instance()

        btnConnect.setOnClickListener {

            textSocketState.text = "Connecting..."

            subs.add(
                client
                    .events()
                    .filter(ConnectedEvent::class.java)
                    .first()
                    .subscribe {
                        Toast.makeText(this, "First connection", Toast.LENGTH_SHORT).show()
                    }
            )

            subs.add(
                client
                    .events()
                    .subscribe {

                        Log.d("evt", it::class.java.simpleName)
                        appendEvent(it)

                        when (it) {
                            is ConnectedEvent -> {
                                textSocketState.text = "Connected"
                            }
                            is ErrorEvent -> {
                                textSocketState.text = "Error: " + it.error.toString()
                            }
                            is ConnectingEvent -> {
                                textSocketState.text = "Connecting..."
                            }
                            is DisconnectedEvent -> {
                                textSocketState.text = "Disconnected"
                            }
                        }
                    }
            )

            client.setUser(User("bender"), token)
        }

        btnDisconnect.setOnClickListener {
            textSocketState.text = "Disconnected"
            client.disconnect()
            subs.forEach { it.unsubscribe() }
            subs.clear()
        }
    }

    override fun onDestroy() {
        subs.forEach { it.unsubscribe() }
        subs.clear()
        super.onDestroy()
    }

    private val sb = StringBuilder()
    private val logTimeFormat = SimpleDateFormat("hh:mm:ss")

    private fun appendEvent(event: ChatEvent) {
        val date = event.receivedAt
        val log = logTimeFormat.format(date) + ":" + event.type + "\n"
        sb.insert(0, log)
        textSocketEvent.text = sb.toString()
    }
}
