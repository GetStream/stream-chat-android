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
import io.getstream.chat.android.client.sample.databinding.ActivitySocketTestsBinding
import io.getstream.chat.android.client.subscribeForSingle
import io.getstream.chat.android.client.utils.observable.Disposable

class SocketTestActivity : AppCompatActivity() {

    var disposables = mutableListOf<Disposable>()

    val token =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYmVuZGVyIn0.3KYJIoYvSPgTURznP8nWvsA2Yj2-vLqrm-ubqAeOlcQ"

    private lateinit var binding: ActivitySocketTestsBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySocketTestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val client = ChatClient.instance()

        binding.btnConnect.setOnClickListener {

            binding.textSocketState.text = "Connecting..."

            disposables.add(
                client.subscribeForSingle<ConnectedEvent> {
                    Toast.makeText(this, "First connection", Toast.LENGTH_SHORT).show()
                }
            )

            disposables.add(
                client.subscribe {
                    Log.d("evt", it::class.java.simpleName)
                    appendEvent(it)

                    binding.textSocketState.text = when (it) {
                        is ConnectedEvent -> {
                            "Connected"
                        }
                        is ErrorEvent -> {
                            "Error: " + it.error.toString()
                        }
                        is ConnectingEvent -> {
                            "Connecting..."
                        }
                        is DisconnectedEvent -> {
                            "Disconnected"
                        }
                        else -> return@subscribe
                    }
                }
            )

            client.setUser(User("bender"), token)
        }

        binding.btnDisconnect.setOnClickListener {
            binding.textSocketState.text = "Disconnected"
            client.disconnect()
            disposables.forEach { it.dispose() }
            disposables.clear()
        }
    }

    override fun onDestroy() {
        disposables.forEach { it.dispose() }
        disposables.clear()
        super.onDestroy()
    }

    private val sb = StringBuilder()

    private fun appendEvent(event: ChatEvent) {
        sb.insert(0, "${event.type}\n")
        binding.textSocketEvent.text = sb.toString()
    }
}
