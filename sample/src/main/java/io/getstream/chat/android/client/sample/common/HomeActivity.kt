package io.getstream.chat.android.client.sample.common

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.sample.R
import kotlinx.android.synthetic.main.activity_home.*
import kotlin.time.ExperimentalTime

@ExperimentalTime
class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        title = ChatClient.instance().getVersion()

        btnSocket.setOnClickListener {
            startActivity(Intent(this, SocketTestActivity::class.java))
        }

        btnChannels.setOnClickListener {
            startActivity(Intent(this, ChannelsListActivity::class.java))
        }

        btnTestChannelsApis.setOnClickListener {
            startActivity(Intent(this, TestChannelsApiMethodsActivity::class.java))
        }

        btnTestUsersApis.setOnClickListener {
            startActivity(Intent(this, TestUsersApiMethodsActivity::class.java))
        }

        btnOneToOne.setOnClickListener {
            startActivity(Intent(this, OneToOneActivity::class.java))
        }
    }
}