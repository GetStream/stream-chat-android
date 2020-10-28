package io.getstream.chat.android.client.sample.common

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.sample.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = ChatClient.instance().getVersion()

        binding.btnSocket.setOnClickListener {
            startActivity(Intent(this, SocketTestActivity::class.java))
        }

        binding.btnChannels.setOnClickListener {
            startActivity(Intent(this, ChannelsListActivity::class.java))
        }

        binding.btnTestChannelsApis.setOnClickListener {
            startActivity(Intent(this, TestChannelsApiMethodsActivity::class.java))
        }

        binding.btnTestUsersApis.setOnClickListener {
            startActivity(Intent(this, TestUsersApiMethodsActivity::class.java))
        }

        binding.btnOneToOne.setOnClickListener {
            startActivity(Intent(this, OneToOneActivity::class.java))
        }

        requestPermissions()
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE),
            1
        )
    }
}
