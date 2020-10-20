package io.getstream.chat.android.client.sample.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.sample.R
import io.getstream.chat.android.client.sample.utils.UserConfig
import kotlinx.android.synthetic.main.activity_push.*

class OneToOneActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_push)

        val members = listOf(userA.userId, userB.userId)
        val local = "http://127.0.0.1:3000"

        commandsViewA.setUser(userA, members, false)
        commandsViewB.setUser(userB, members, false)
    }

    override fun onDestroy() {
        commandsViewA.destroy()
        commandsViewB.destroy()
        super.onDestroy()
    }
}
