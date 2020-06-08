package io.getstream.chat.android.client.sample.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.iid.FirebaseInstanceId
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.sample.App
import io.getstream.chat.android.client.sample.R
import io.getstream.chat.android.client.sample.utils.UserConfig
import io.getstream.chat.android.client.sample.utils.UtilsMessages
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.observable.Subscription
import kotlinx.android.synthetic.main.activity_push.*

class PushActivity : AppCompatActivity() {

    private val subs = mutableListOf<Subscription>()
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

        commandsViewA.setUser(userA, members, true)
        commandsViewB.setUser(userB, members, true)
    }

    override fun onDestroy() {
        commandsViewA.destroy()
        commandsViewB.destroy()
        super.onDestroy()
    }

    private fun queryFirstChannel() {
        val filter = FilterObject("type", "messaging")
        val sort = QuerySort().asc("created_at")
        App.client.queryChannels(QueryChannelsRequest(filter, 0, 1, sort)).enqueue {
            if (it.isSuccess) {

            }
        }
    }
}