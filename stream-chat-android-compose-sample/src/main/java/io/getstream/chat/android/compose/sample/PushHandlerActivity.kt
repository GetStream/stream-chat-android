package io.getstream.chat.android.compose.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.TaskStackBuilder

class PushHandlerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.hasExtra(KEY_CHANNEL_ID)) {
            val channelId = intent.getStringExtra(KEY_CHANNEL_ID)!!

            TaskStackBuilder.create(this)
                .addNextIntent(ChannelActivity.getIntent(this))
                .addNextIntent(MessagesActivity.getIntent(this, channelId))
                .startActivities()
        }
        finish()
    }

    companion object {
        private const val KEY_CHANNEL_ID = "channelId"

        fun getIntent(context: Context, channelId: String): Intent {
            return Intent(context, PushHandlerActivity::class.java).apply {
                putExtra(KEY_CHANNEL_ID, channelId)
            }
        }
    }
}
