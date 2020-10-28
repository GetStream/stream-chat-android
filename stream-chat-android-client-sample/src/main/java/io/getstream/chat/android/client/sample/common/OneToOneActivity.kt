package io.getstream.chat.android.client.sample.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.sample.databinding.ActivityPushBinding
import io.getstream.chat.android.client.sample.utils.UserConfig

class OneToOneActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPushBinding

    private val clients = mutableListOf<ChatClient>()

    val userA = UserConfig(
        "stream-eugene",
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoic3RyZWFtLWV1Z2VuZSJ9.-WNauu6xV56sHM39ZrhxDeBiKjA972O5AYo-dVXva6I",
        "d2q3juekvgsf"
    )

    val userB = UserConfig(
        "stream-eugene-2",
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoic3RyZWFtLWV1Z2VuZS0yIn0.A2mlC05oUHaEiyogoB2OePR25KGWy2yJ9CLEYp80kJw",
        "d2q3juekvgsf"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPushBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val members = listOf(userA.userId, userB.userId)
        val local = "http://127.0.0.1:3000"

        binding.commandsViewA.setUser(userA, members, false)
        binding.commandsViewB.setUser(userB, members, false)
    }

    override fun onDestroy() {
        binding.commandsViewA.destroy()
        binding.commandsViewB.destroy()
        super.onDestroy()
    }
}
