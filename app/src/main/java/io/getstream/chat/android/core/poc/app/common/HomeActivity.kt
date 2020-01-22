package io.getstream.chat.android.core.poc.app.common

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.core.poc.R
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

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
            Toast.makeText(this, "Undefined", Toast.LENGTH_SHORT).show()
        }
    }
}